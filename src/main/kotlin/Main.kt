package com.alvla.tcp_port_forwarder

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*

suspend fun handleStream(readStream: ByteReadChannel, writeStream: ByteWriteChannel) = coroutineScope {
    launch {
        try {
            while (!readStream.isClosedForRead) {
                val transBytes = readStream.copyTo(writeStream)
                println("Transferred from server to client: $transBytes")
            }
        } catch (e: Throwable) {
            println("Server read channel error: ${e.message}")
        }
    }
}

suspend fun handleSession(srcSock: Socket, targetHost: String, targetPort: Int) = coroutineScope {
    val selectorManager = SelectorManager(Dispatchers.IO)
    val dstSock = aSocket(selectorManager).tcp().connect(targetHost, targetPort)

    launch {
        handleStream(srcSock.openReadChannel(), dstSock.openWriteChannel(autoFlush = true))
    }.invokeOnCompletion {
        println("Source host connection closed")
        srcSock.close()
    }

    launch {
        handleStream(dstSock.openReadChannel(), srcSock.openWriteChannel(autoFlush = true))
    }.invokeOnCompletion {
        println("Target host connection closed")
        dstSock.close()
        selectorManager.close()
    }
}

data class Config(val sourceHost: String, val sourcePort: Int, val targetHost: String, val targetPort: Int)

fun main(args: Array<String>) = runBlocking {
    val config = Config("127.0.0.1", args[0].toInt(), args[1], args[2].toInt())
    val selectorManager = SelectorManager(Dispatchers.IO)
    val serverSocket = aSocket(selectorManager).tcp().bind(config.sourceHost, config.sourcePort)

    println("Server is listening at ${serverSocket.localAddress}")

    while (true) {
        val socket = serverSocket.accept()
        println("Accepted $socket")
        handleSession(socket, config.targetHost, config.targetPort)
    }
}