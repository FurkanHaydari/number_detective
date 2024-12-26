fun View.fadeIn() {
    alpha = 0f
    visibility = View.VISIBLE
    animate()
        .alpha(1f)
        .setDuration(300)
        .setInterpolator(AccelerateDecelerateInterpolator())
        .start()
}

fun View.pulseAnimation() {
    animate()
        .scaleX(1.1f)
        .scaleY(1.1f)
        .setDuration(100)
        .withEndAction {
            animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(100)
                .start()
        }
        .start()
} 