import androidx.compose.ui.window.ComposeUIViewController
import me.tbsten.prac.scrollbartest.App
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController { App() }
