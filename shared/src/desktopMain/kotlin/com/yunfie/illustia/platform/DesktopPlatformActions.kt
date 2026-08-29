package com.yunfie.illustia.platform

import java.awt.Desktop
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.net.URI

class DesktopPlatformActions : PlatformActions {
    override fun openUrl(url: String) {
        runCatching {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI.create(url))
            }
        }
    }

    override fun copyToClipboard(text: String, label: String) {
        runCatching {
            val selection = StringSelection(text)
            Toolkit.getDefaultToolkit().systemClipboard.setContents(selection, selection)
        }
    }

    override fun shareText(text: String, title: String) {
        copyToClipboard(text, title)
    }

    override fun showToast(message: String) {
        println("[Toast] $message")
    }

    override fun getPlatformName(): String = "Desktop"

    override fun getOsVersion(): String =
        System.getProperty("os.name", "Desktop") + " " + System.getProperty("os.version", "")

    override fun getUserAgent(): String =
        "PixivDesktopApp/5.5.0 (${getOsVersion()}; Java ${System.getProperty("java.version")})"

    override fun isDynamicColorSupported(): Boolean = false
}
