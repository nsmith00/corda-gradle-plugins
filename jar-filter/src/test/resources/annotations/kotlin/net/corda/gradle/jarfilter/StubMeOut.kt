@file:Suppress("PackageDirectoryMismatch")
package net.corda.gradle.jarfilter

import kotlin.annotation.AnnotationRetention.*
import kotlin.annotation.AnnotationTarget.*

@Target(
    CONSTRUCTOR,
    FUNCTION,
    PROPERTY_GETTER,
    PROPERTY_SETTER
)
@Retention(RUNTIME)
annotation class StubMeOut