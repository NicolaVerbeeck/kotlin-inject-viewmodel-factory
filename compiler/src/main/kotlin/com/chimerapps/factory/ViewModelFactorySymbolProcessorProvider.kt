package com.chimerapps.factory

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class ViewModelFactorySymbolProcessorProvider : SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {

        val pkg = environment.options["viewModelFactoryPackage"]
        val parent = environment.options["viewModelFactoryParent"]

        if (pkg == null) {
            environment.logger.error("viewModelFactoryPackage is not set")
        }
        if (parent == null) {
            environment.logger.error("viewModelFactoryParent is not set")
        }

        return ViewModelFactorySymbolProcessor(
            environment.codeGenerator,
            pkg ?: "",
            parent ?: "",
        )
    }

}