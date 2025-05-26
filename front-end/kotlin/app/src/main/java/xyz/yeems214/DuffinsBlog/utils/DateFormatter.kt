package xyz.yeems214.DuffinsBlog.utils

import java.text.SimpleDateFormat
import java.util.*

object DateFormatter {
    
    /**
     * Formats a date string to the desired format: "May 24, 2025 at 3:55pm"
     * Handles multiple input formats including ISO with microseconds and milliseconds
     */
    fun formatPostDate(dateString: String?): String {
        if (dateString.isNullOrBlank()) return "Unknown date"
        
        // List of possible date formats from the backend
        val possibleFormats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSS",        // ISO with microseconds (6 digits)
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",        // ISO with milliseconds and Z
            "yyyy-MM-dd'T'HH:mm:ss'Z'",            // ISO without milliseconds
            "yyyy-MM-dd'T'HH:mm:ss.SSS",           // ISO with milliseconds, no Z
            "yyyy-MM-dd'T'HH:mm:ss",               // ISO basic format
            "yyyy-MM-dd HH:mm:ss",                 // SQL datetime format
        )
        
        val outputFormat = SimpleDateFormat("MMM dd, yyyy 'at' h:mma", Locale.getDefault())
        
        for (format in possibleFormats) {
            try {
                val inputFormat = SimpleDateFormat(format, Locale.getDefault())
                inputFormat.timeZone = TimeZone.getTimeZone("UTC") // Assume UTC input
                
                val date = inputFormat.parse(dateString)
                date?.let {
                    return outputFormat.format(it).lowercase()
                        .replace("am", "am")
                        .replace("pm", "pm")
                }
            } catch (e: Exception) {
                // Continue to next format
                continue
            }
        }
        
        // If all parsing fails, return the original string
        return dateString
    }
}
