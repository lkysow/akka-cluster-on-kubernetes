package com.hootsuite.akkak8s

import java.net.InetAddress

import akka.actor.Actor

class Backend extends Actor {
  val hostname = InetAddress.getLocalHost.getHostName
  def receive = {
    case NotHotDogRequest(message) =>
      if (message.toLowerCase.contains("hotdog")) {
        sender() ! NotHotDogResponse(true, hostname)
      } else {
        sender() ! NotHotDogResponse(false, hostname)
      }
  }
}
