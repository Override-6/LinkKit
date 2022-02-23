/*
 * Copyright (c) 2021. Linkit and or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR FILE HEADERS.
 *
 * This code is free software; you can only use it for personal uses, studies or documentation.
 * You can download this source code, and modify it ONLY FOR PERSONAL USE and you
 * ARE NOT ALLOWED to distribute your MODIFIED VERSION.
 * For any professional use, please contact me at overridelinkit@gmail.com.
 *
 * Please contact overridelinkit@gmail.com if you need additional information or have any
 * questions.
 */

package fr.linkit.engine.gnom.cache.sync.contract.descriptor

import fr.linkit.api.gnom.cache.sync.contract.{FieldContract, RemoteObjectInfo, StructureContractDescriptor, ValueContract}
import fr.linkit.api.gnom.cache.sync.contract.behavior.RemoteInvocationRule
import fr.linkit.api.gnom.cache.sync.contract.behavior.annotation.BasicInvocationRule
import fr.linkit.api.gnom.cache.sync.contract.description.{FieldDescription, MethodDescription, SyncStructureDescription}
import fr.linkit.api.gnom.cache.sync.contract.descriptors.{ContractDescriptorData, MethodContractDescriptor}
import fr.linkit.api.gnom.cache.sync.contract.modification.ValueModifier
import fr.linkit.api.internal.concurrency.Procrastinator
import fr.linkit.engine.gnom.cache.sync.contract.behavior.AnnotationBasedMemberBehaviorFactory
import fr.linkit.engine.gnom.cache.sync.contract.descriptor.ContractDescriptorDataBuilder.{MethodBehaviorBuilder, Recognizable}
import fr.linkit.engine.gnom.cache.sync.contractv2.{FieldContractImpl, SimpleValueContract}
import fr.linkit.engine.gnom.cache.sync.invokation.GenericRMIRulesAgreementBuilder

import java.lang.reflect.{Field, Method, Parameter}
import java.util.NoSuchElementException
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

abstract class ContractDescriptorDataBuilder {

    private val builders = mutable.ListBuffer.empty[ClassDescriptor[_]]

    def describe[C <: AnyRef](builder: ClassDescriptor[C]): Unit = {
        builders += builder
    }

    def build(): ContractDescriptorData = {
        var descriptions = builders.map(_.getResult).toArray
        if (!descriptions.exists(_.targetClass eq classOf[Object])) {
            //Adding descriptor for the object class that is essential.
            descriptions :+= new StructureContractDescriptor[Object] {
                override val targetClass     : Class[Object]                    = classOf[Object]
                override val remoteObjectInfo: Option[RemoteObjectInfo]         = None
                override val methods         : Array[MethodContractDescriptor]  = Array.empty
                override val fields          : Array[(Int, FieldContract[Any])] = Array.empty
                override val modifier        : Option[ValueModifier[Object]]    = None
            }
        }
        new ContractDescriptorDataImpl(descriptions)
    }

    abstract class ClassDescriptor[A <: AnyRef](override val tag: Option[Any])(implicit desc: SyncStructureDescription[A]) extends Recognizable {

        def this()(implicit desc: SyncStructureDescription[A]) {
            this(None)(desc)
        }

        private val clazz                        = desc.clazz
        private val methodContracts              = mutable.HashMap.empty[Method, MethodContractDescriptor]
        private val fieldContracts               = mutable.HashMap.empty[Field, FieldContractImpl[Any]]
        protected var modifier: ValueModifier[A] = _

        private var result: StructureContractDescriptor[_] = _

        def enable: MemberEnable = new MemberEnable

        def disable: MemberDisable = new MemberDisable

        class MemberDisable {

            def field(name: String): Unit = {
                val fDesc = desc.findFieldDescription(name).get
                fieldContracts.put(fDesc.javaField, new FieldContractImpl[Any](fDesc, None, false))
            }

            def method(name: String)(params: Class[_]*): Unit = {
                val mDesc              = desc.findMethodDescription(name, params).get
                val contractDescriptor = MethodContractDescriptorImpl(mDesc, null, null, Array.empty, false, false, new GenericRMIRulesAgreementBuilder())
                methodContracts.put(mDesc.javaMethod, contractDescriptor)
            }

            def allMethods(): Unit = {
                desc.listMethods().foreach { mDesc =>
                    val contractDescriptor = MethodContractDescriptorImpl(mDesc, null, null, Array.empty, false, false, new GenericRMIRulesAgreementBuilder())
                    methodContracts.put(mDesc.javaMethod, contractDescriptor)
                }
            }

            def allFields(): Unit = {

            }
        }

        class MemberEnable {

            class MethodBehaviorDescriptorBuilderIntroduction(descs: Array[MethodDescription]) {

                private var concluded = false

                def and(methodName: String)(params: Class[_]*): MethodBehaviorDescriptorBuilderIntroduction = {
                    new MethodBehaviorDescriptorBuilderIntroduction(descs :+ getMethod(methodName, params))
                }

                def as(methodName: String)(params: Class[_]*): Unit = conclude {
                    val bhv = methodContracts.getOrElse(getMethod(methodName, params).javaMethod, {
                        throw new NoSuchElementException(s"Method '$methodName' not described. its behavior must be described before.")
                    })
                    descs.map(new MethodContractDescriptorImpl(_, bhv))
                }

                def as(builder: MethodBehaviorBuilder): Unit = conclude {
                    descs.map(desc => {
                        builder.setContext(desc)
                        builder.build()
                    })
                }

                def withRule(rule: RemoteInvocationRule): Unit = conclude {
                    descs.map(desc => {
                        val builder = rule(new GenericRMIRulesAgreementBuilder())
                        MethodContractDescriptorImpl(desc, null, null, Array.empty, false, false, builder)
                    })
                }

                private def conclude(conclusion: => Array[MethodContractDescriptorImpl]): Unit = {
                    if (concluded)
                        throw new IllegalStateException("This method was already described.")
                    conclusion.foreach { contractDescriptor =>
                        methodContracts.put(contractDescriptor.description.javaMethod, contractDescriptor)
                    }
                    concluded = true
                }

            }

            class FieldBehaviorDescriptorBuilderIntroduction(descs: Array[FieldDescription]) {
                //TODO be able to build field behaviors
            }

            private def getMethod(name: String, params: Seq[Class[_]]): MethodDescription = {
                desc.findMethodDescription(name, params).getOrElse {
                    throw new NoSuchElementException(s"Can not find declared or inherited method '$name' in $clazz, is this method final or private ?")
                }
            }

            private def getField(name: String): FieldDescription = {
                desc.findFieldDescription(name).getOrElse {
                    throw new NoSuchElementException(s"Can not find declared or inherited field '$name' in $clazz")
                }
            }

            def method(name: String)(params: Class[_]*): MethodBehaviorDescriptorBuilderIntroduction = {
                val mDesc = getMethod(name, params)
                new MethodBehaviorDescriptorBuilderIntroduction(Array(mDesc))
            }

            def field[F <: AnyRef](name: String)(modifier: ValueModifier[F]): Unit = {
                val fDesc    = getField(name)
                val contract = new FieldContractImpl[F](fDesc, Some(modifier), true)
                fieldContracts.put(fDesc.javaField, contract.asInstanceOf[FieldContractImpl[Any]])
            }
        }

        private[ContractDescriptorDataBuilder] def getResult: StructureContractDescriptor[_] = {
            if (result != null)
                return result
            result = new StructureContractDescriptor[A] {
                override val targetClass     : Class[A]                         = desc.clazz
                override val remoteObjectInfo: Option[RemoteObjectInfo]         = None
                override val methods         : Array[MethodContractDescriptor]  = methodContracts.values.toArray
                override val fields          : Array[(Int, FieldContract[Any])] = fieldContracts.values.map(x => (x.desc.fieldId, x)).toArray
                override val modifier        : Option[ValueModifier[A]]         = Option(ClassDescriptor.this.modifier)
            }
            result
        }
    }

}

object ContractDescriptorDataBuilder {

    sealed trait Recognizable {

        val tag: Option[Any]
    }

    abstract class MethodBehaviorBuilder(rule: RemoteInvocationRule = BasicInvocationRule.ONLY_CURRENT) extends AbstractBehaviorBuilder[MethodDescription] {

        private val paramContract              = mutable.HashMap.empty[Parameter, ValueContract[Any]]
        private var usedParams: Option[params] = None

        private var forceLocalInvocation: Boolean        = false
        private var procrastinator      : Procrastinator = _

        def mustForceLocalInvocation(): Unit = forceLocalInvocation = true

        def withProcrastinator(procrastinator: Procrastinator): Unit = {
            this.procrastinator = procrastinator
        }

        //def withRule(rule: RemoteInvocationRule): Unit = rules = Array(rule)

        object returnvalue {

            var enabled = true
            private[MethodBehaviorBuilder] var modifier: ValueModifier[Any] = _

            def withModifier[R](modifier: ValueModifier[R]): Unit = {
                if (this.modifier != null)
                    throw new IllegalStateException("Return Value modifier already set !")
                this.modifier = modifier.asInstanceOf[ValueModifier[Any]]
            }
        }

        abstract class params {

            if (usedParams.isDefined)
                throw new IllegalStateException(s"'params' descriptor is already set.")
            usedParams = Some(this)

            private val assignements = ListBuffer.empty[As]

            class As(override val tag: Option[Any]) extends Recognizable {

                private[params] var assignedBehavior: ValueContract[Any] = _

                def as(paramName: String): Unit = {
                    assignedBehavior = getParamBehavior(paramName)(s"Parameter '$paramName' not described. Its behavior must be described before.")
                }

                def as(idx: Int): Unit = {
                    assignedBehavior = getParamBehavior(idx)(s"Parameter at index '$idx' not described. Its behavior must be described before.")
                }
            }

            def enable[P <: AnyRef](paramName: String)(paramModifier: ValueModifier[P]): Unit = callOnceContextSet {
                val param = getParam(paramName)
                val bhv   = new SimpleValueContract[P](true, Option(paramModifier))
                paramContract.put(param, bhv.asInstanceOf[ValueContract[Any]])
            }

            def enable[P <: AnyRef](idx: Int, paramModifier: ValueModifier[P]): Unit = callOnceContextSet {
                val param = getParam(idx)
                val bhv   = new SimpleValueContract[P](true, Option(paramModifier))
                paramContract.put(param, bhv.asInstanceOf[ValueContract[Any]])
            }

            def enable(paramName: String): As = {
                val as = new As(Some(paramName))
                assignements += as
                as
            }

            def enable(idx: Int): As = {
                val as = new As(Some(idx))
                assignements += as
                as
            }

            private[MethodBehaviorBuilder] def concludeAllAssignements(): Unit = {
                for (as <- assignements) {
                    val param = as.tag match {
                        case Some(idx: Int)     => getParam(idx)
                        case Some(name: String) => getParam(name)
                    }
                    val bhv   = as.assignedBehavior
                    paramContract.put(param, bhv)
                }
            }
        }

        private[ContractDescriptorDataBuilder] def build(): MethodContractDescriptorImpl = {
            usedParams.foreach(_.concludeAllAssignements()) //will modify the paramBehaviors map
            val jMethod             = context.javaMethod
            val parameterContracts  = getParamContracts(jMethod)
            val returnValueContract = new ValueContract[Any] {
                override val isSynchronized: Boolean                    = returnvalue.enabled
                override val modifier      : Option[ValueModifier[Any]] = Option(returnvalue.modifier)
            }
            val builder             = new GenericRMIRulesAgreementBuilder()
            rule(builder)
            MethodContractDescriptorImpl(context, procrastinator, returnValueContract, parameterContracts, false, forceLocalInvocation, builder)
        }

        private def getParamContracts(jMethod: Method): Array[ValueContract[Any]] = {
            val base = jMethod.getParameters.map(getOrDefaultContract)
            if (base.exists(pb => pb.modifier != null || pb.isSynchronized))
                base
            else Array.empty
        }

        private def getOrDefaultContract(parameter: Parameter): ValueContract[Any] = {
            paramContract.getOrElse(parameter, {
                val bhv = AnnotationBasedMemberBehaviorFactory.genParameterContract(parameter)
                new SimpleValueContract(bhv.isSynchronized, bhv.modifier)
            })
        }

        private def getParam(paramName: String): Parameter = {
            val method = context.javaMethod
            val params = method.getParameters
            params.find(_.getName == paramName).getOrElse {
                throw new NoSuchElementException(s"No parameter named '$paramName' found for method $method. (method's parameter names are ${params.mkString("[", ", ", "]")})")
            }
        }

        private def getParam(idx: Int): Parameter = {
            context.javaMethod.getParameters()(idx)
        }

        private def getParamBehavior(name: String)(noSuchMsg: String): ValueContract[Any] = {
            val param = getParam(name)
            if (param.getType.isPrimitive)
                throw new UnsupportedOperationException("can't synchronize or apply modifiers on primitive values.")
            paramContract.getOrElse(param, {
                throw new NoSuchElementException(noSuchMsg)
            })
        }

        private def getParamBehavior(idx: Int)(noSuchMsg: String): ValueContract[Any] = {
            val param = getParam(idx)
            if (param.getType.isPrimitive)
                throw new UnsupportedOperationException("can't synchronize or apply modifiers on primitive values.")
            paramContract.getOrElse(param, {
                throw new NoSuchElementException(noSuchMsg)
            })
        }

    }

}
