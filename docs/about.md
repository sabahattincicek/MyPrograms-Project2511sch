---
layout: default
title: About MyPrograms
---

<div style="margin-bottom: 20px;">
  <a href="./" style="text-decoration: none; color: #0366d6; display: flex; align-items: center; gap: 5px; font-weight: bold;">
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
      <line x1="19" y1="12" x2="5" y2="12"></line>
      <polyline points="12 19 5 12 12 5"></polyline>
    </svg>
    Back to Support Home
  </a>
</div>

---

<div align="center">
  <img src="./assets/logo.png" alt="MyPrograms Logo" width="150">
  <h1>MyPrograms</h1>
  <p><strong>The Ultimate Local-First Academic Planner for Students</strong></p>
</div>

---

## ℹ️ What is MyPrograms?

**MyPrograms** is a powerful, privacy-focused academic assistant designed specifically for students who want to take full control of their educational life. Unlike complex, cloud-heavy enterprise tools, MyPrograms follows a **"Local-First"** philosophy: everything you enter stays on your device, ensuring maximum speed and total privacy.

---

## 📸 Screenshots
<p align="center">
  <!-- TODO: Ekran görüntülerini eklediğinde buradaki yorumları kaldır ve linkleri güncelle -->
  <!-- <img src="./assets/ss_home.png" width="30%" /> -->
  <!-- <img src="./assets/ss_schedule.png" width="30%" /> -->
  <!-- <img src="./assets/ss_files.png" width="30%" /> -->
  <br>
  <i>(Visuals coming soon - A clean, modern UI experience)</i>
</p>

---

## 🚀 Key Features (What can you do?)

Based on our advanced **Domain-Driven Architecture**, MyPrograms offers features that go beyond a simple calendar:

### 📅 Smart Schedule Management
Using our advanced `RecurrenceRule` logic, the app doesn't just store events; it calculates your academic patterns. Whether it's a weekly lecture or a one-time exam, the app generates a dynamic timeline for you.

### 📁 Integrated Study Materials
Through the `SFile` system and Android's **Storage Access Framework (SAF)**, you can link your lecture notes, PDFs, and images directly to your courses. Access your study materials exactly where and when you need them.

### 🏷️ Active Filtering & Tags
Organize your life using Tags. You can mute entire categories of courses or focus only on what's important today. The `GetHomeDisplayItemsUseCase` ensures that your home screen only shows what you care about.

### 📱 Jetpack Glance Widgets
Stay on top of your day without even opening the app. Our modern **Compose-based widgets** bring your schedule directly to your home screen with a beautiful, responsive design.

---

## 🛠️ Technical Vision (How it works)
- **Local Database:** Powered by **Room (SQLite)** for permanent, lightning-fast storage.
- **Clean Architecture:** Built with UseCases, Repositories, and ViewModels to ensure a crash-free, maintainable experience.
- **Privacy:** No external servers, no tracking. Your data is your own.

---

## 🗺️ Future Roadmap (What's next?)

We are constantly evolving! Here is what we are planning for the next major updates:

1.  **☁️ Cloud Sync (Firebase):** Optional account creation and cross-device synchronization for those who want to access their schedule on multiple devices.
2.  **🤝 Peer Sharing:** A secure way to share your course schedules or study materials with your classmates.
3.  **📊 Academic Analytics:** Visual charts to track your exam performance and study habits over time.
4.  **🎨 More Customization:** Advanced theme engines and more widget styles to match your personal aesthetic.

---

## 👥 Who is it for?
- **University Students** with complex, recurring weekly schedules.
- **High Schoolers** preparing for major exams.
- **Lifelong Learners** who need a structured way to manage their study files and progress.

---

<p align="center" style="margin-top: 50px; font-size: 0.9em; color: #8b949e;">
  Developed with ❤️ for students. <br>
  © 2026 MyPrograms Development.
</p>