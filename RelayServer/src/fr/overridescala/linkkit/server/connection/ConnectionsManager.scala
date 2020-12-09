package fr.overridescala.linkkit.server.connection

import fr.overridescala.linkkit.api.exceptions.{RelayException, RelayInitialisationException}
import fr.overridescala.linkkit.api.system.{JustifiedCloseable, Reason}
import fr.overridescala.linkkit.server.RelayServer

import scala.collection.mutable

/**
 * TeamMate of RelayServer, handles the RelayPoint Connections.
 *
 * @see [[RelayServer]]
 * @see [[ClientConnection]]
 * */
class ConnectionsManager(server: RelayServer) extends JustifiedCloseable {
    /**
     * java map containing all RelayPointConnection instances
     * */
    private val connections: mutable.Map[String, ClientConnection] = mutable.Map.empty

    override def close(reason: Reason): Unit = {
        for ((_, connection) <- connections)
            connection.close(reason)
    }

    /**
     * creates and register a RelayPoint connection.
     *
     * @param socket the socket to start the connection
     * @throws RelayInitialisationException when a id is already set for this address, or another connection is known under this id.
     * */
    def register(socket: SocketContainer, identifier: String): Unit = {
        if (connections.contains(identifier))
            throw RelayInitialisationException(s"This relay id is already registered ! ('$identifier')")

        if (connections.size > server.configuration.maxConnection)
            throw new RelayException("Maximum connection limit exceeded")

        val connection = ClientConnection.open(socket, server, identifier)
        println(s"Relay Point connected with identifier '${connection.identifier}'")
        connections.put(identifier, connection)
    }

    /**
     * unregisters a Relay point
     *
     * @param identifier the identifier to disconnect
     * */
    def unregister(identifier: String): Unit = {
        connections.remove(identifier)
    }


    /**
     * retrieves a RelayPointConnection based on the address
     *
     * @param identifier the identifier linked [[ClientConnection]]
     * @return the found [[ClientConnection]] bound with the identifier
     * */
    def getConnectionFromIdentifier(identifier: String): ClientConnection = {
        for ((_, connection) <- connections) {
            val connectionIdentifier = connection.identifier
            if (connectionIdentifier.equals(identifier))
                return connection
        }
        null
    }

    /**
     * determines if the address is not registered
     *
     * @param identifier the identifier to test
     * @return true if the address is not registered, false instead
     * */
    def isNotRegistered(identifier: String): Boolean = {
        !connections.contains(identifier)
    }

    /**
     * @param identifier the identifier to test
     * @return true if any connected Relay have the specified identifier
     * */
    def containsIdentifier(identifier: String): Boolean = {
        for (connection <- connections.values) {
            val connectionIdentifier = connection.identifier
            if (connectionIdentifier.equals(identifier))
                return true
        }
        identifier == server.identifier //blacklists server identifier
    }


    /**
     * Deflects a packet to his associated [[ClientConnection]]
     *
     * @throws RelayException if no connection where found for this packet.
     * @param bytes the packet bytes to deflect
     * */
    private[connection] def deflectTo(bytes: Array[Byte], target: String): Unit = {
        val connection = getConnectionFromIdentifier(target)
        if (connection == null)
            throw new RelayException(s"unknown ID '$target' to deflect packet")
        connection.sendDeflectedBytes(bytes)
    }


}
