
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.ladycure.ui.theme.ThemeManager
import com.example.ladycure.ui.theme.Typography


@Composable
fun DefaultColorPalette() = lightColorScheme(
    background = DefaultBackground,
    primary = DefaultPrimary,
    primaryContainer = DefaultPrimaryVariant,
    secondary = DefaultSecondary,
    secondaryContainer = DefaultSecondaryVariant
)

@Composable
fun redColorPalette() = lightColorScheme(
    background = RedBackground,
    primary = RedPrimary,
    primaryContainer = RedPrimaryVariant,
    secondary = RedSecondary,
    secondaryContainer = RedSecondaryVariant
)

@Composable
fun blueColorPalette() = lightColorScheme(
    background = BlueBackground,
    primary = BluePrimary,
    primaryContainer = BluePrimaryVariant,
    secondary = BlueSecondary,
    secondaryContainer = BlueSecondaryVariant
)

@Composable
fun greenColorPalette() = lightColorScheme(
    background = GreenBackground,
    primary = GreenPrimary,
    primaryContainer = GreenPrimaryVariant,
    secondary = GreenSecondary,
    secondaryContainer = GreenSecondaryVariant
)

@Composable
fun aquaColorPalette() = lightColorScheme(
    background = AquaBackground,
    primary = AquaPrimary,
    primaryContainer = AquaPrimaryVariant,
    secondary = AquaSecondary,
    secondaryContainer = AquaSecondaryVariant
)

@Composable
fun purpleColorPalette() = lightColorScheme(
    background = PurpleBackground,
    primary = PurplePrimary,
    primaryContainer = PurplePrimaryVariant,
    secondary = PurpleSecondary,
    secondaryContainer = PurpleSecondaryVariant
)

@Composable
fun LadyCureTheme(
content: @Composable () -> Unit
) {
    // Always use the default color scheme
    val colorScheme = DefaultColorPalette()

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
