package ua.turskyi.domain.exceptions

class NetworkErrorException(override val message: String = "network error occurred") :
    Exception(message)