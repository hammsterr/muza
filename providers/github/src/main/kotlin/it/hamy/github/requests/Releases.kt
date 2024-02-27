package it.hamy.github.requests

import io.ktor.client.call.body
import io.ktor.client.request.get
import it.hamy.extensions.runCatchingCancellable
import it.hamy.github.GitHub
import it.hamy.github.models.Release

suspend fun GitHub.releases(
    owner: String,
    repo: String,
    page: Int = 1,
    pageSize: Int = 30
) = runCatchingCancellable {
    httpClient.get("repos/$owner/$repo/releases") {
        withPagination(page = page, size = pageSize)
    }.body<List<Release>>()
}
