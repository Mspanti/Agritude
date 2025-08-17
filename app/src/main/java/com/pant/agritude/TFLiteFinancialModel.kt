package com.pant.agritude

import android.content.Context
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.nio.MappedByteBuffer

data class FinancialPrediction(
    val production: Float,
    val yieldValue: Float,
    val profit: Float
)

class TFLiteFinancialModel(private val context: Context) {

    private var interpreter: Interpreter? = null


    private val scalerCenter = floatArrayOf(
        171081.0f, 171080.0f, 2009.0f, 20.0f, 354.0f, 213.0f, 647.0f, 368.0f, 560.0f, 8.1f, 9.7f, 11.5f, 21.9f, 54.7f, 130.7f, 291.8f, 266.8f, 165.3f, 61.6f, 5.6f, 3.0f, 57840.0f, 0.0f)
    private val scalerScale = floatArrayOf(
        172473.0f, 172473.0f, 12.0f, 18.0f, 323.0f, 602.0f, 5416.0f, 87.0f, 1525.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 4322320.0f, 1.0f)


    fun initialize() {
        try {
            val options = Interpreter.Options()
            options.setNumThreads(4)
            interpreter = Interpreter(loadModelFile(), options)
            Log.d("TFLiteFinancialModel", "TFLite model initialized successfully.")
        } catch (e: Exception) {
            Log.e("TFLiteFinancialModel", "Error initializing TFLite model", e)
        }
    }

    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd("agritude_financial_model.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    // `predict` ஃபங்க்ஷன் இப்போது `List<Float>`-ஐ உள்ளீடாகப் பெறுகிறது.
    fun predict(userInput: List<Float>): FinancialPrediction? {
        if (interpreter == null) {
            Log.e("TFLiteFinancialModel", "Interpreter is not initialized.")
            return null
        }


        // உள்ளீட்டு அளவை dynamic-ஆக மாடலின் உள்ளீட்டு வடிவத்துடன் சரிபார்க்கவும்.
        val inputShape = interpreter?.getInputTensor(0)?.shape()
        val expectedSize = inputShape?.getOrNull(1) ?: 140

        if (userInput.size != expectedSize) {
            Log.e("TFLiteFinancialModel", "Input size mismatch. Expected $expectedSize, got ${userInput.size}")
            return null
        }

        val normalizedInput = FloatArray(userInput.size)
        for (i in userInput.indices) {
            if (scalerScale[i] != 0.0f) {
                normalizedInput[i] = (userInput[i] - scalerCenter[i]) / scalerScale[i]
            } else {
                normalizedInput[i] = 0.0f
            }
        }

        val inputByteBuffer = ByteBuffer.allocateDirect(userInput.size * 4).order(ByteOrder.nativeOrder())
        inputByteBuffer.asFloatBuffer().put(normalizedInput)

        val outputArray = Array(1) { FloatArray(3) }

        try {
            interpreter?.run(inputByteBuffer, outputArray)
            val output = outputArray[0]
            val production = output[0]
            val yieldValue = output[1]
            val profit = output[2]
            return FinancialPrediction(production, yieldValue, profit)
        } catch (e: Exception) {
            Log.e("TFLiteFinancialModel", "Error during inference", e)
            return null
        }
    }

    fun close() {
        interpreter?.close()
        Log.d("TFLiteFinancialModel", "Interpreter closed.")
    }
}
