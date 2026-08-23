const form = document.getElementById('planForm');
const btn = form.querySelector('.submit');
const results = document.getElementById('results');
const emptyState = document.getElementById('emptyState');
const errorState = document.getElementById('errorState');
const errorMessage = document.getElementById('errorMessage');
const itemsEl = document.getElementById('items');

const checked = (name) => form.querySelector(`input[name="${name}"]:checked`).value;

// --- NEW: PHASE 4 PROFILE AUTO-LOAD ---
// Runs automatically when the webpage loads
document.addEventListener("DOMContentLoaded", function() {
    // --- DARK MODE TOGGLE LOGIC ---
    const themeToggle = document.getElementById('themeToggle');
    const savedTheme = localStorage.getItem('theme');

    // 1. Check for saved preference or OS default on page load
    if (savedTheme === 'dark' || (!savedTheme && window.matchMedia('(prefers-color-scheme: dark)').matches)) {
        document.documentElement.classList.add('dark');
        if (themeToggle) themeToggle.textContent = '☀️';
    } else {
        if (themeToggle) themeToggle.textContent = '🌙';
    }

    // 2. Listen for clicks on the toggle button
    if (themeToggle) {
        themeToggle.addEventListener('click', () => {
            const isDark = document.documentElement.classList.toggle('dark');
            localStorage.setItem('theme', isDark ? 'dark' : 'light');
            themeToggle.textContent = isDark ? '☀️' : '🌙';
        });
    }
    fetch('/profile')
        .then(response => response.text())
        .then(text => {
            if (!text) return null; // If server sends nothing (no profile), stop here
            return JSON.parse(text);
        })
        .then(profile => {
            if (profile) {
                // Populate the text inputs
                document.getElementById('weight').value = profile.weight || '';
                document.getElementById('height').value = profile.height || '';
                document.getElementById('age').value = profile.age || '';

                // Populate the dropdown
                if (profile.sex) document.getElementById('sex').value = profile.sex;

                // Check the correct radio buttons
                if (profile.activity) {
                    const activityRadio = form.querySelector(`input[name="activity"][value="${profile.activity}"]`);
                    if (activityRadio) activityRadio.checked = true;
                }
                if (profile.goal) {
                    const goalRadio = form.querySelector(`input[name="goal"][value="${profile.goal}"]`);
                    if (goalRadio) goalRadio.checked = true;
                }
            }
        })
        .catch(err => console.error("Could not load saved profile:", err));
});
// --- END NEW ---


// --- EXISTING FORM SUBMISSION LOGIC ---
form.addEventListener('submit', function (e) {
    e.preventDefault();
    if (!form.reportValidity()) return;

    const payload = {
        weight: parseFloat(document.getElementById('weight').value),
        height: parseFloat(document.getElementById('height').value),
        age: parseInt(document.getElementById('age').value),
        sex: document.getElementById('sex').value,
        activity: checked('activity'),
        goal: checked('goal')
    };

    btn.classList.add('is-loading');
    btn.disabled = true;

    fetch('/generate-plan', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    })
        .then(response => {
            if (!response.ok) throw new Error('Server responded with ' + response.status);
            return response.json();
        })
        .then(data => {
            itemsEl.innerHTML = '';
            (data.items || []).forEach((item, i) => {
                const rounded = Math.round(item.portions * 2) / 2; // nearest 0.5
                if (rounded <= 0) return;
                const li = document.createElement('li');
                li.className = 'item';
                li.style.animationDelay = (i * 60) + 'ms';
                li.innerHTML = `
                        <span class="portion">${rounded}</span>
                        <span class="item-body">
                            <strong>${item.name}</strong>
                            <small>${rounded} ${item.unit}${rounded !== 1 ? 's' : ''}</small>
                        </span>`;
                itemsEl.appendChild(li);
            });

            if (!itemsEl.children.length) {
                const li = document.createElement('li');
                li.className = 'item';
                li.innerHTML = '<span class="item-body"><strong>No portions returned</strong><small>Try different inputs</small></span>';
                itemsEl.appendChild(li);
            }

            document.getElementById('statCalories').textContent = Math.round(data.totalCalories) + ' kcal';
            document.getElementById('statProtein').textContent = Number(data.totalProtein).toFixed(1) + ' g';

            emptyState.hidden = true;
            errorState.hidden = true;
            results.hidden = false;
        })
        .catch(err => {
            errorMessage.textContent = err.message || 'Something went wrong.';
            results.hidden = true;
            emptyState.hidden = true;
            errorState.hidden = false;
        })
        .finally(() => {
            btn.classList.remove('is-loading');
            btn.disabled = false;
        });
});