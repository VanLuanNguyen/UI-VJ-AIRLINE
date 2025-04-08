package com.vietjoke.vn.Activities.Splash

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.window.SplashScreen
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusRequester.Companion
import androidx.compose.ui.focus.FocusRequester.Companion.createRefs
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.vietjoke.vn.Activities.Login.LoginActivity
import com.vietjoke.vn.Activities.SearchFlight.SearchFlightActivity
import com.vietjoke.vn.MainActivity
import com.vietjoke.vn.R

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent{
            val sharedPreferences: SharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
            val isFirstTime = sharedPreferences.getBoolean("isFirstTime", true)

            if (isFirstTime) {
                sharedPreferences.edit().putBoolean("isFirstTime", false).apply()
                SplashScreen(onGetStartedClick = {
                    startActivity(Intent(this, SearchFlightActivity::class.java))
                })
            } else {
                startActivity(Intent(this, SearchFlightActivity::class.java))
                finish()
            }
        }
    }
}

@Composable
@Preview
fun SplashScreen(onGetStartedClick:()-> Unit={}){
    StatusTopBarColor()
    Column(modifier = Modifier.fillMaxSize()) {
        ConstraintLayout() {
            val(backgroundImg,title,subtitle,startbtn) = createRefs()
            Image(
                painter = painterResource(R.drawable.splash_bg),
                contentDescription = null,
                modifier = Modifier
                    .constrainAs(backgroundImg){
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                    }
                    .fillMaxSize()
            )
            val styledText= buildAnnotatedString {
                append("Discover your\nDream")
                withStyle(style = SpanStyle(color = colorResource(R.color.orange))){
                    append(" Flight")
                }
                append("\nEasily")
            }
            Text(
                text = styledText,
                fontSize = 53.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .padding(top = 32.dp)
                    .padding(horizontal = 16.dp)
                    .constrainAs(title){
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                    }
            )

            Text(
                text = stringResource(R.string.subtitle_splash),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorResource(R.color.orange),
                modifier = Modifier
                    .padding(top=32.dp, start = 16.dp)
                    .constrainAs(subtitle){
                        top.linkTo(title.bottom)
                        start.linkTo(title.start)
                    }
            )
            Box(modifier = Modifier.constrainAs(startbtn){
                bottom.linkTo(parent.bottom)
            }){
                GradientButton(onClick = onGetStartedClick, "Get Started", 32)
            }
        }
    }
}

@Composable
fun StatusTopBarColor(){
    val systemUiController = rememberSystemUiController()

    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.Transparent,
            darkIcons = false
        )
    }
}