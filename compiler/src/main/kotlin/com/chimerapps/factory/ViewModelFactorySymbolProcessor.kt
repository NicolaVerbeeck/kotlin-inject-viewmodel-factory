package com.chimerapps.factory

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import java.io.Writer

/**
 * @author Nicola Verbeeck
 */
class ViewModelFactorySymbolProcessor(
    private val codeGenerator: CodeGenerator,
    private val targetPackage: String,
    private val parentComponent: String,
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (targetPackage.isEmpty() || parentComponent.isEmpty()) return emptyList()

        val symbols = resolver.getSymbolsWithAnnotation("com.chimerapps.kotlin.inject.factory.AndroidViewModel")
        val classes = symbols.mapTo(mutableListOf()) { symbol -> (symbol as KSClassDeclaration) }

        if (classes.isEmpty()) return emptyList()

        val dependencies = Dependencies(aggregating = true, *classes.map { it.containingFile!! }.toTypedArray())

        val output = codeGenerator.createNewFile(dependencies, targetPackage, "GeneratedViewModelFactory", "kt")

        output.bufferedWriter().use {
            generateCode(classes, it)
        }

        return emptyList()
    }

    private fun generateCode(classes: MutableList<KSClassDeclaration>, target: Writer) {
        val file = FileSpec.builder(targetPackage, "GeneratedViewModelFactory")
            .addType(
                TypeSpec.classBuilder("GeneratedViewModelFactory")
                    .addAnnotation(ClassName.bestGuess("me.tatarka.inject.annotations.Component"))
                    .addAnnotation(ClassName.bestGuess("me.tatarka.inject.annotations.Inject"))
                    .addModifiers(KModifier.ABSTRACT, KModifier.PUBLIC)
                    .primaryConstructor(
                        FunSpec.constructorBuilder()
                            .addParameter(
                                ParameterSpec.builder("parent", ClassName.bestGuess(parentComponent))
                                    .addAnnotation(ClassName.bestGuess("me.tatarka.inject.annotations.Component")).build()
                            )
                            .build()
                    )
                    .addProperty(
                        PropertySpec.builder("parent", ClassName.bestGuess(parentComponent))
                            .initializer("parent")
                            .build()
                    )
                    .also {
                        classes.forEach { viewModelClass ->
                            it.addFunction(
                                FunSpec.builder("provide${viewModelClass.simpleName.asString()}")
                                    .addModifiers(KModifier.ABSTRACT, KModifier.PUBLIC)
                                    .returns(ClassName.bestGuess(viewModelClass.qualifiedName!!.asString()))
                                    .build()
                            )
                        }
                    }
                    .addFunction(generateCreatorFunction(classes))
                    .build()
            )
            .build()

        file.writeTo(object : Appendable {
            override fun append(csq: CharSequence?): Appendable {
                csq?.toString()?.let(target::write)
                return this
            }

            override fun append(csq: CharSequence?, start: Int, end: Int): Appendable {
                csq?.subSequence(start, end)?.toString()?.let(target::write)
                return this
            }

            override fun append(c: Char): Appendable {
                target.write(c.toString())
                return this
            }

        })
    }

    private fun generateCreatorFunction(classes: MutableList<KSClassDeclaration>): FunSpec {
        val generic = TypeVariableName.invoke("T", ClassName.bestGuess("androidx.lifecycle.ViewModel"))

        return FunSpec.builder("createInstance")
            .addAnnotation(AnnotationSpec.builder(ClassName.bestGuess("kotlin.Suppress")).addMember("\"UNCHECKED_CAST\"").build())
            .addTypeVariable(generic)
            .returns(generic)
            .addParameter(ParameterSpec.builder("ref", ClassName.bestGuess("kotlin.reflect.KClass").parameterizedBy(generic)).build())
            .addCode(CodeBlock.builder()
                .add("return ")
                .beginControlFlow("when")
                .also { codeBlock ->
                    classes.forEach { viewModelClass ->
                        codeBlock.add(
                            "%T::class == ref -> provide%T() as T", ClassName.bestGuess(viewModelClass.qualifiedName!!.asString()),
                            ClassName.bestGuess(viewModelClass.qualifiedName!!.asString())
                        )
                        codeBlock.add("\n")
                    }
                    codeBlock.add("else -> throw IllegalArgumentException(%S)\n","Unknown ViewModel class: \${ref.java.name} (did you forget to annotate it with AndroidViewModel?)")
                }
                .endControlFlow()
                .build())
            .build()
    }

}