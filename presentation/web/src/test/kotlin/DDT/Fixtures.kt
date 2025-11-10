package DDT

import zettai.Zettai
import zettai.prepareToDoListHubForTests

fun prepareZettaiForTests(): Zettai =
    Zettai(prepareToDoListHubForTests())
