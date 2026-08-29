package com.yunfie.illustia

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource as composeStringResource

object R {
    typealias string = Res.string
    typealias drawable = Res.drawable
}

@Composable
fun stringResource(resource: StringResource): String = composeStringResource(resource)

@Composable
fun stringResource(resource: StringResource, vararg formatArgs: Any): String =
    composeStringResource(resource, *formatArgs)
