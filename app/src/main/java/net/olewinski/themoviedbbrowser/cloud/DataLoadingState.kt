package net.olewinski.themoviedbbrowser.cloud

/**
 * Status of request for fetching data
 */
enum class RequestStatus {
    /**
     * Data is still being loaded
     */
    LOADING,

    /**
     * Data loaded successfully
     */
    SUCCESS,

    /**
     * Failure while loading data
     */
    FAILURE
}

/**
 * Container representing status of request for fetching data together with any related message
 *
 * @param requestStatus [RequestStatus] representing status itself
 * @param errorMessage  Any message related to given status
 */
data class DataLoadingState constructor(
    val requestStatus: RequestStatus,
    val errorMessage: String? = null
) {
    /**
     * Ready-to-use instances + helpful method for obtaining instance representing failure with message
     */
    companion object {
        val LOADING = DataLoadingState(RequestStatus.LOADING)
        val SUCCESS = DataLoadingState(RequestStatus.SUCCESS)

        fun error(errorMessage: String?) = DataLoadingState(RequestStatus.FAILURE, errorMessage)
    }
}
