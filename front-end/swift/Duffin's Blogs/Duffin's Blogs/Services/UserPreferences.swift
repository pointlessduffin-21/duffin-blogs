import Foundation

class UserPreferences: ObservableObject {
    static let shared = UserPreferences()
    
    @Published var use24HourFormat: Bool {
        didSet {
            UserDefaults.standard.set(use24HourFormat, forKey: "use24HourFormat")
        }
    }
    
    private init() {
        // Check if user has set a preference, otherwise use system default
        if UserDefaults.standard.object(forKey: "use24HourFormat") != nil {
            self.use24HourFormat = UserDefaults.standard.bool(forKey: "use24HourFormat")
        } else {
            // Use system locale to determine default format
            let formatter = DateFormatter()
            formatter.timeStyle = .short
            let sampleTime = formatter.string(from: Date())
            self.use24HourFormat = !sampleTime.contains("AM") && !sampleTime.contains("PM")
        }
    }
    
    func formatDate(_ date: Date) -> String {
        let dateFormatter = DateFormatter()
        dateFormatter.dateStyle = .medium
        dateFormatter.timeStyle = .none
        
        return dateFormatter.string(from: date)
    }
    
    func formatTime(_ date: Date) -> String {
        let timeFormatter = DateFormatter()
        timeFormatter.dateStyle = .none
        
        if use24HourFormat {
            timeFormatter.timeStyle = .short
            timeFormatter.locale = Locale(identifier: "en_GB") // Force 24-hour format
        } else {
            timeFormatter.timeStyle = .short
            timeFormatter.locale = Locale(identifier: "en_US") // Force 12-hour format with AM/PM
        }
        
        return timeFormatter.string(from: date)
    }
    
    func formatDateTime(_ date: Date) -> (date: String, time: String) {
        return (formatDate(date), formatTime(date))
    }
}
