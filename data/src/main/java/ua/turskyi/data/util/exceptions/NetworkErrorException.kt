package ua.turskyi.data.util.exceptions

class NetworkErrorException(override val message: String = "network error occurred") :
    Exception(message)