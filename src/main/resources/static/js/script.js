// src/main/resources/static/js/scripts.js

document.addEventListener('DOMContentLoaded', () => {
    // --- 1️⃣ Sidebar Date Section ---
    const today = new Date();
    const currentDayEl = document.getElementById("currentDay");
    const currentDateEl = document.getElementById("currentDate");

    if (currentDayEl && currentDateEl) {
        currentDayEl.textContent = today.toLocaleDateString('en-US', { weekday: 'long' });
        currentDateEl.textContent = today.toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'long',
            day: 'numeric'
        });
    }

    // --- 2️⃣ Daily Quote Section ---
    const quotes = [
        "The secret of getting ahead is getting started. — Mark Twain",
        "Do something today that your future self will thank you for. — Unknown",
        "Success is the sum of small efforts repeated day in and day out. — Robert Collier",
        "Don't watch the clock; do what it does. Keep going. — Sam Levenson",
        "The way to get started is to quit talking and begin doing. — Walt Disney"
    ];

    const quoteEl = document.getElementById("dailyQuote");
    if (quoteEl) {
        const randomIndex = Math.floor(Math.random() * quotes.length);
        quoteEl.textContent = quotes[randomIndex];
    }
});
