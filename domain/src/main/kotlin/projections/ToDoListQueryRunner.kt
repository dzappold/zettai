package projections

import events.ToDoListEvent

class ToDoListQueryRunner(eventFetcher: FetchStoredEvents<ToDoListEvent>) : QueryRunner<ToDoListQueryRunner> {
    internal val listProjection = ToDoListProjection(eventFetcher)
    internal val itemProjection = ToDoItemProjection(eventFetcher)

    override fun <R> invoke(f: ToDoListQueryRunner.() -> R): ProjectionQuery<R> =
        ProjectionQuery(setOf(listProjection, itemProjection)) { f(this) }
}
