package com.cesoft.cesrunner.ui.aiagent
/*
import ai.koog.agents.core.tools.Tool
import ai.koog.agents.core.tools.annotations.LLMDescription
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SealedSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ArraySerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.jvm.JvmInline

object CalculatorTools {

    abstract class CalculatorTool(
        override val name: String,
        override val description: String,
    ) : Tool<CalculatorTool.Args, CalculatorTool.Result>() {
        @Serializable
        data class Args(
            @property:LLMDescription("First number")
            val a: Float,
            @property:LLMDescription("Second number")
            val b: Float
        ) {
            companion object {
                @OptIn(SealedSerializationApi::class)
                fun serializer(): KSerializer<Args> {
                    return object:KSerializer<Args> {
                        override val descriptor: SerialDescriptor
                            get() = object: SerialDescriptor {
                                override val serialName: String
                                    get() = ""
                                override val kind: SerialKind
                                    get() = SerialKind.ENUM
                                override val elementsCount: Int
                                    get() = 0

                                override fun getElementName(index: Int): String {
                                    return ""
                                }

                                override fun getElementIndex(name: String): Int {
                                    return 0
                                }

                                override fun getElementAnnotations(index: Int): List<Annotation> {
                                    return listOf()
                                }

                                override fun getElementDescriptor(index: Int): SerialDescriptor {
                                    TODO("Not yet implemented")
                                }

                                override fun isElementOptional(index: Int): Boolean {
                                    TODO("Not yet implemented")
                                }
                            }

                        override fun serialize(
                            encoder: Encoder,
                            value: Args
                        ) { }

                        override fun deserialize(decoder: Decoder): Args {
                            return Args(0f, 0f)
                        }
                    }
                }
            }
        }

        @Serializable
        @JvmInline
        value class Result(val result: Float) {
            companion object {
                @OptIn(SealedSerializationApi::class)
                fun serializer(): KSerializer<Result> {
                    return TODO("Provide the return value")
                }
            }
        }

        final override val argsSerializer: KSerializer<Args> = Args.serializer()
        override val resultSerializer: KSerializer<Result> = Result.serializer()
    }

    object PlusTool : CalculatorTool(
        name = "plus",
        description = "Adds a and b",
    ) {
        override suspend fun execute(args: Args): Result {
            return Result(args.a + args.b)
        }
    }
}
*/