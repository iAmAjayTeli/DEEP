# DataBinding
-keepclassmembers class * extends androidx.databinding.BaseObservable {
    <fields>;
    <methods>;
}

# Retrofit
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**
-keep class kotlin.coroutines.Continuation { *; }

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Firebase
-keepattributes *Annotation*
-keepclassmembers class * {
    @com.google.firebase.encoders.annotations.Encodable public *;
}
-keep class com.google.firebase.components.** { *; }
-dontwarn com.google.firebase.**
-keep class com.google.firebase.** { *; }


# Lottie
-keep class com.airbnb.lottie.** { *; }

# Maps and Places API
-keep class com.google.android.gms.maps.** { *; }
-dontwarn com.google.android.gms.maps.**
-keep class com.google.android.gms.common.** { *; }
-dontwarn com.google.android.gms.common.**
-keep class com.google.android.gms.location.** { *; }
-dontwarn com.google.android.gms.location.**
-keep class com.google.maps.android.** { *; }
-dontwarn com.google.maps.android.**

# Logging Interceptor
-dontwarn okhttp3.logging.**
-keep class okhttp3.logging.** { *; }

# Suppress warnings for unused Android libraries
-dontwarn android.location.Geocoder

# Model classes
-keep class com.deep.eyedonation.model.** { *; }
-keep class com.deep.eyedonation.view.user.EyeBankScreen {*;}
-keep class com.deep.eyedonation.adapter.EyeBankAdapter {*;}
-keep class com.deep.eyedonation.api.** {*;}
# Keep adapter classes
-keep class com.deep.eyedonation.adapter.** { *; }
-keep class com.deep.eyedonation.model.PlacesResponse { *; }



-keepclassmembers class com.deep.eyedonation.view.user.EyeBankScreen {
    <methods>;
    <fields>;
}

# Please add these rules to your existing keep rules in order to suppress warnings.
# This is generated automatically by the Android Gradle plugin.
-dontwarn org.bouncycastle.jsse.BCSSLParameters
-dontwarn org.bouncycastle.jsse.BCSSLSocket
-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
-dontwarn org.conscrypt.Conscrypt$Version
-dontwarn org.conscrypt.Conscrypt
-dontwarn org.conscrypt.ConscryptHostnameVerifier
-dontwarn org.openjsse.javax.net.ssl.SSLParameters
-dontwarn org.openjsse.javax.net.ssl.SSLSocket
-dontwarn org.openjsse.net.ssl.OpenJSSE

# Keep the BuildConfig class to ensure API keys are not stripped out
-keep class **.BuildConfig { *; }

# Prevent warnings related to the BuildConfig file
-dontwarn **.BuildConfig

-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

-keepattributes Signature
-keepattributes RuntimeVisibleAnnotations
-keep class retrofit2.** { *; }
-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**


