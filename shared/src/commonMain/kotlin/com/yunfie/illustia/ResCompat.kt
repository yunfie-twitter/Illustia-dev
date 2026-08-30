package com.yunfie.illustia

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource as composeStringResource

internal object R {
    val string get() = Res.string
    val drawable get() = Res.drawable
}

@Composable
fun stringResource(resource: StringResource): String = composeStringResource(resource)

@Composable
fun stringResource(resource: StringResource, vararg formatArgs: Any): String =
    composeStringResource(resource, *formatArgs)
