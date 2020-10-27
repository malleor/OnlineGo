package io.zenandroid.onlinego.utils
import java.security.MessageDigest

object WhatsNewUtils {

    val shouldDisplayDialog: Boolean
        get() = PersistenceManager.previousWhatsNewTextHashed != null && PersistenceManager.previousWhatsNewTextHashed != hashString(currentText)
    val whatsNewText = currentText

    fun textShown() {
        PersistenceManager.previousWhatsNewTextHashed = hashString(currentText)
    }

    private fun hashString(text: String): String {
        return MessageDigest.getInstance("MD5").digest(text.toByteArray(Charsets.UTF_8)).fold("", { str, it -> str + "%02x".format(it) })
    }
}

private const val currentText = """
## What's new

## Fix

This is a hotfix for the KataGO not starting up on some phones with a file not found error.
Sorry for the double release.

## New Feature

You can now play GO offline against the KataGO AI. This is still experimental and I've only really tested it on a Samsung Galaxy S9+. Please raise an issue including the phone's model [here](https://github.com/acristescu/OnlineGo/issues) if it does not work for you.

The bot should play at around 3-4 dan, so most people would require a handicap for an enjoyable game. In the future we're hoping to implement a way to "dumb down" the bot in the style of the Katrain app.

## About project

This is an open-source project. To find out how you can contribute or support the project, head over to the project’s [Github page](https://github.com/acristescu/OnlineGo).
"""