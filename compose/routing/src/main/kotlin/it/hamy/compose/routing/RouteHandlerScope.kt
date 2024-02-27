package it.hamy.compose.routing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable

@Stable
class RouteHandlerScope(
    val route: Route?,
    val parameters: Array<Any?>,
    private val push: (Route?) -> Unit,
    val pop: () -> Unit
) {
    @Composable
    inline fun NavHost(content: @Composable () -> Unit) {
        if (route == null) content()
    }

    operator fun Route.invoke() = push(this)

    operator fun <P0> Route.invoke(p0: P0) {
        parameters[0] = p0
        invoke()
    }

    operator fun <P0, P1> Route.invoke(p0: P0, p1: P1) {
        parameters[1] = p1
        invoke(p0)
    }

    operator fun <P0, P1, P2> Route.invoke(p0: P0, p1: P1, p2: P2) {
        parameters[2] = p2
        invoke(p0, p1)
    }
}
