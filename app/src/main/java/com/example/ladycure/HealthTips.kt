package com.example.ladycure

import java.util.Calendar
import kotlin.random.Random

object HealthTips {
    val tips = listOf(
        "🦋 Butterflies taste with feet – try eating one meal mindfully daily, savoring each flavor like it's nectar.",
        "Take the stairs instead of the elevator when possible.",
        "Get 7–9 hours of sleep each night to allow your body to rest and recover.",
        "🦘 Kangaroo move: Do 10 mini-squats every 2 hours – your pouch (core) will get stronger!",
        "Limit processed foods and opt for whole, natural foods instead.",
        "Practice proper lifting techniques to protect your back.",
        "🐘 Elephant alert: They walk 10+ miles daily – mimic them with a 30-minute walk to strengthen your “memory trunk”!",
        "Warm up before exercising to prevent injuries.",
        "Wash your hands frequently to prevent the spread of germs.",
        "🐧 Penguins propose with pebbles – collect small joyful moments each day to build your happiness nest.",
        "Practice mindfulness for at least 5 minutes each day.",
        "Include probiotic foods in your diet for gut health.",
        "Practice good posture to prevent back and neck pain.",
        "🐢 Turtle truth: Slow down sometimes! Try 5 minutes of deep breathing like you're sunbathing on a log.",
        "Schedule regular health check-ups with your doctor.",
        "Take short breaks every hour if you sit for long periods.",
        "🦦 Like otters holding hands while sleeping, make time to cuddle loved ones – physical touch reduces stress hormones!",
        "Manage stress through meditation, deep breathing, or other relaxation techniques.",
        "Challenge negative thoughts with positive affirmations.",
        "🦔 Hedgehog wisdom: Sometimes curling up with tea and a book is the best self-care (just don’t hibernate all winter!).",
        "Practice gratitude daily to improve mental wellbeing.",
        "Eat a rainbow of colorful fruits and vegetables daily.",
        "Get your eyes checked regularly.",
        "Spend time in nature to reduce stress.",
        "Stretch regularly to maintain flexibility and prevent injuries.",
        "🐾 Meerkat magic: Stand tall like their sentry pose hourly to fix posture and reduce back pain!",
        "Learn to say no to maintain healthy boundaries.",
        "Wear sunscreen daily to protect your skin from harmful UV rays.",
        "Try a new physical activity to keep exercise interesting.",
        "Limit screen time before bed to improve sleep quality.",
        "🦉 Take owl breaks: Every hour, do 30-second neck rolls (like owls’ 270° vision) to prevent tech neck.",
        "Regular exercise can improve your mood and reduce stress. Aim for 30 minutes a day.",
        "Keep a journal to process your thoughts and emotions.",
        "🐿️ Squirrel prep: Store healthy snacks (nuts, fruits) in your bag instead of raiding vending machines.",
        "Set realistic fitness goals to stay motivated.",
        "Choose whole grains over refined grains for better nutrition.",
        "Listen to your body and rest when needed.",
        "Stay hydrated and drink at least 8 glasses of water daily for optimal health.",
        "Floss daily to maintain good oral health and prevent gum disease.",
        "🦜 Parrot power: Chat with friends daily – social connections boost immunity like vitamins!",
        "Stay hydrated with herbal teas as well as water.",
        "Maintain social connections for better mental health.",
        "🐬 Dolphins sleep with half their brain – but you need full rest! Aim for 7–9 hours to recharge like a baby dolphin."
    )

    fun getDailyTip(): String {
        val today = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        Random(today.toLong()).let {
            return tips[it.nextInt(tips.size)]
        }
    }

    fun getRandomTip(): String {
        return tips[Random.nextInt(tips.size)]
    }
}