package com.baro.dispatch.interfaces.rest

object DispatchApiPaths {
    const val DISPATCH = "/dispatch"
    const val PRE_DISPATCH = "/pre"
    const val PRE_DISPATCH_FULL = "$DISPATCH$PRE_DISPATCH"
    const val CONFIRM_DISPATCH_FULL = DISPATCH
    const val COMMAND_ACK = "/command-ack"
    const val COMMAND_ACK_FULL = "$DISPATCH$COMMAND_ACK"
    const val ARRIVED = "/arrived"
    const val ARRIVED_FULL = "$DISPATCH$ARRIVED"
    const val ACTIVE_VEHICLE = "/vehicles/{carId}/active"
    const val ACTIVE_VEHICLE_PATTERN = "/dispatch/vehicles/*/active"
    const val CANCEL_DISPATCH = "/{dispatchId}/cancel"
    const val CANCEL_DISPATCH_FULL = "$DISPATCH$CANCEL_DISPATCH"
    const val INTERNAL_DISPATCH = "/internal/dispatch"
    const val EXPORT_DAILY_REQUESTS = "/export/daily/requests"
    const val EXPORT_DAILY_REQUESTS_FULL = "$INTERNAL_DISPATCH$EXPORT_DAILY_REQUESTS"
    const val EXPORT_DAILY_DISPATCHES = "/export/daily/dispatches"
    const val EXPORT_DAILY_DISPATCHES_FULL = "$INTERNAL_DISPATCH$EXPORT_DAILY_DISPATCHES"
}
