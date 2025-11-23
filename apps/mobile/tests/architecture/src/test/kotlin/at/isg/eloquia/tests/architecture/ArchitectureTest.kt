package at.isg.eloquia.tests.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.jupiter.api.Test

class ArchitectureTest {

    @Test
    fun `classes with 'UseCase' suffix should reside in 'domain' package`() {
        Konsist
            .scopeFromProject()
            .classes()
            .withNameEndingWith("UseCase")
            .assertTrue {
                it.resideInPackage("..domain..")
            }
    }

    @Test
    fun `classes with 'Repository' suffix should reside in 'data' or 'domain' package`() {
        Konsist
            .scopeFromProject()
            .classes()
            .withNameEndingWith("Repository")
            .assertTrue {
                it.resideInPackage("..domain..") || it.resideInPackage("..data..")
            }
    }

    @Test
    fun `classes with 'ViewModel' suffix should reside in 'presentation' package`() {
        Konsist
            .scopeFromProject()
            .classes()
            .withNameEndingWith("ViewModel")
            .assertTrue {
                it.resideInPackage("..presentation..")
            }
    }
}
