package fr.linkit.engine.internal.language.bhv.interpreter

import fr.linkit.api.application.ApplicationContext
import fr.linkit.api.gnom.cache.sync.contract.descriptor.ContractDescriptorData
import fr.linkit.engine.internal.language.bhv.PropertyClass

trait LangContractDescriptorData extends ContractDescriptorData {
    /**
     * The language source content
     * */
    val source: String
    /**
     * property class that goes with the language source.
     * */
    val propertyClass: PropertyClass

    val app: ApplicationContext
}