package com.example.data.model

data class Country(
    val name: String,
    val code: String,
    val flagEmoji: String,
    val dialCode: String,
    val exampleNumber: String
)

object CountryList {
    val all = listOf(
        Country("United States", "US", "🇺🇸", "+1", "202-555-0143"),
        Country("United Kingdom", "GB", "🇬🇧", "+44", "7911 123456"),
        Country("India", "IN", "🇮🇳", "+91", "98765 43210"),
        Country("Canada", "CA", "🇨🇦", "+1", "416-555-0199"),
        Country("Australia", "AU", "🇦🇺", "+61", "412 345 678"),
        Country("Germany", "DE", "🇩🇪", "+49", "151 2345678"),
        Country("France", "FR", "🇫🇷", "+33", "6 12 34 56 78"),
        Country("Japan", "JP", "🇯🇵", "+81", "90-1234-5678"),
        Country("Brazil", "BR", "🇧🇷", "+55", "11 91234-5678"),
        Country("Mexico", "MX", "🇲🇽", "+52", "55 1234 5678"),
        Country("Singapore", "SG", "🇸🇬", "+65", "8123 4567"),
        Country("United Arab Emirates", "AE", "🇦🇪", "+971", "50 123 4567"),
        Country("Spain", "ES", "🇪🇸", "+34", "612 34 56 78"),
        Country("Italy", "IT", "🇮🇹", "+39", "312 345 6789"),
        Country("Philippines", "PH", "🇵🇭", "+63", "917 123 4567")
    )
}
