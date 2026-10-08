# Retro Parallax Player

I built this to see if I could recreate the classic iPod click-wheel in Android using custom touch gestures, and then decided to hook it up to the phone's hardware sensors so the whole UI tilts in 3D space as you move the device.

<img src="https://github.com/user-attachments/assets/a1154542-838c-4574-b111-7127cfbc0553" width="160" alt="5"> <img src="https://github.com/user-attachments/assets/4810d5de-be23-49b1-ade1-eb3ec3207727" width="160" alt="4"> <img src="https://github.com/user-attachments/assets/5a1f173b-42c9-478a-b6dd-c51a3ff5cdea" width="160" alt="2"> <img src="https://github.com/user-attachments/assets/e4ee20e7-5642-4404-bac4-4376ec12a0a4" width="160" alt="1"> <img src="https://github.com/user-attachments/assets/3277649d-3091-42bf-9ade-6856780e93e4" width="160" alt="0">

https://github.com/user-attachments/assets/0c3df27c-ea06-4749-9f24-f13911290f5b





### What's going on under the hood?
* **Custom Click-Wheel:** I mapped circular drag gestures using `atan2` math to translate thumb rotations into scrolling and navigation. It includes haptic feedback so it actually feels like a physical wheel.
* **Hardware 3D Tilt:** Uses the device's gravity sensor to calculate pitch and roll. The entire player UI dynamically blurs, scales, and rotates based on the physical angle of the phone.
* **Color Extraction:** Hooked up the Palette API so the background gradient smoothly morphs to match the dominant colors of whatever album art is currently playing.
* **Media Engine:** Fully integrated with `ExoPlayer` for the audio backend, complete with a spinning vinyl/CD animation for the currently playing track.

### Tech Stack
* Kotlin & Jetpack Compose
* `SensorManager` for gravity/tilt tracking
* `ExoPlayer` for media playback
* Coil + Palette API for image loading and dynamic theming
* Custom Canvas drawing for the UI components

### Running the App
Clone it and fire it up in Android Studio. 
*Note:* You really need to run this on a physical phone with local mp3 files to actually experience the tilt effect and haptic wheel. The emulator won't do it justice.
