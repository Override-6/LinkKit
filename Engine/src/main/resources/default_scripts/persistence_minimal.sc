import fr.linkit.api.application.ApplicationContext
import fr.linkit.engine.gnom.network.{NetworkDataBundle, NetworkDataTrunk}
import fr.linkit.engine.gnom.packet.fundamental.EmptyPacket
import fr.linkit.engine.gnom.persistence.config.PersistenceConfigBuilder
import fr.linkit.engine.gnom.persistence.defaults._
import fr.linkit.engine.internal.language.bhv.interpreter.LangContractDescriptorData
import fr.linkit.engine.internal.language.bhv.{Contract, PropertyClass}
import fr.linkit.engine.internal.utils.Identity

import java.nio.file.Path

//Start Of Context
val builder: PersistenceConfigBuilder = null

import builder._

//ENd Of Context

putContextReference(1, EmptyPacket)
putContextReference(2, Identity(Nil))
putContextReference(3, None)
setTConverter[Path, String](_.toString)(Path.of(_))
setTConverter[NetworkDataTrunk, NetworkDataBundle](_.toBundle)(NetworkDataTrunk.fromData)
setTConverter[LangContractDescriptorData, (ApplicationContext, String, PropertyClass)](d => (d.app, d.fileName, d.propertyClass)){case (app, name, p) => Contract(name, app, p)}
//putPersistence(new ScalaIterableTypePersistence)
//putPersistence(new ScalaMapTypePersistence)
putPersistence(new JavaArrayListTypePersistence)
putPersistence(new JavaHashMapTypePersistence)
putPersistence(new JavaHashSetTypePersistence)
//setTConverter[File, String](_.getAbsolutePath)(new File(_))
