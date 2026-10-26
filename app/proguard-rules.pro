# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Strip debug/verbose logs from release builds (LLM responses include run locations)
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
}

# Gson: models serialized by reflection
-keep class com.cesoft.cesrunner.domain.entity.RunDto { *; }
-keep class com.cesoft.cesrunner.domain.entity.AIAgentRes { *; }
-keep class com.cesoft.cesrunner.ui.aiagent.ai.RunEntity { *; }
-keep class com.cesoft.cesrunner.ui.aiagent.ai.RunEntity$* { *; }
-keep class io.github.vyfor.groqkt.api.** { *; }
-keepattributes Signature, *Annotation*, InnerClasses, EnclosingMethod
-keep class * extends com.google.gson.reflect.TypeToken

# Koog: ToolSet methods are discovered with kotlin-reflect via @Tool / @LLMDescription
-keep class com.cesoft.cesrunner.ui.aiagent.ai.RunsToolSet { *; }
-keep class kotlin.Metadata { *; }

# JVM/server-only classes referenced by transitive Koog deps (netty, lettuce, ktor debug), unused on Android
-dontwarn io.micrometer.context.ContextAccessor
-dontwarn java.lang.management.ManagementFactory
-dontwarn java.lang.management.RuntimeMXBean
-dontwarn javax.enterprise.inject.spi.Extension
-dontwarn org.apache.log4j.Level
-dontwarn org.apache.log4j.Logger
-dontwarn org.apache.log4j.Priority
-dontwarn org.apache.logging.log4j.LogManager
-dontwarn org.apache.logging.log4j.Logger
-dontwarn org.apache.logging.log4j.message.MessageFactory
-dontwarn org.apache.logging.log4j.spi.ExtendedLogger
-dontwarn org.apache.logging.log4j.spi.ExtendedLoggerWrapper
-dontwarn reactor.blockhound.integration.BlockHoundIntegration
