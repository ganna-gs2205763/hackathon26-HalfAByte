/**
 * Main application logic for the SMS simulator.
 */
(function() {
    'use strict';

    // State
    let currentPhone = '';
    let pollingInterval = null;
    let lastOutboxCount = 0;

    // DOM Elements
    const elements = {
        phoneSelect: document.getElementById('phone-select'),
        newPhoneInput: document.getElementById('new-phone'),
        addPhoneBtn: document.getElementById('add-phone-btn'),
        currentPhone: document.getElementById('current-phone'),
        chatMessages: document.getElementById('chat-messages'),
        messageInput: document.getElementById('message-input'),
        sendBtn: document.getElementById('send-btn'),
        commandButtons: document.querySelectorAll('.cmd-btn'),
        refreshOutboxBtn: document.getElementById('refresh-outbox-btn'),
        resetBtn: document.getElementById('reset-btn'),
        outboxMessages: document.getElementById('outbox-messages'),
    };

    /**
     * Initialize the application.
     */
    async function init() {
        // Bind event handlers
        elements.phoneSelect.addEventListener('change', onPhoneSelect);
        elements.addPhoneBtn.addEventListener('click', onAddPhone);
        elements.newPhoneInput.addEventListener('keypress', e => {
            if (e.key === 'Enter') onAddPhone();
        });
        elements.sendBtn.addEventListener('click', onSendMessage);
        elements.messageInput.addEventListener('keypress', e => {
            if (e.key === 'Enter') onSendMessage();
        });
        elements.refreshOutboxBtn.addEventListener('click', refreshOutbox);
        elements.resetBtn.addEventListener('click', onReset);

        // Bind quick command buttons
        elements.commandButtons.forEach(btn => {
            btn.addEventListener('click', () => {
                if (!currentPhone) {
                    showToast('Please select or add a phone number first');
                    return;
                }
                elements.messageInput.value = btn.dataset.cmd;
                elements.messageInput.focus();
            });
        });

        // Load initial data
        await loadDevices();
        await refreshOutbox();

        // Start polling for outbox updates
        startPolling();
    }

    /**
     * Show a toast notification.
     */
    function showToast(message) {
        // Simple alert for now - could be replaced with a nicer toast
        alert(message);
    }

    /**
     * Load known devices and populate the phone selector.
     */
    async function loadDevices() {
        try {
            const devices = await SimulatorAPI.getDevices();

            elements.phoneSelect.innerHTML = '<option value="">Select a phone...</option>';

            devices.forEach(device => {
                const option = document.createElement('option');
                option.value = device.phoneNumber;
                option.textContent = `${device.phoneNumber} (${device.messageCount} msgs)`;
                elements.phoneSelect.appendChild(option);
            });

            if (currentPhone && devices.some(d => d.phoneNumber === currentPhone)) {
                elements.phoneSelect.value = currentPhone;
            }
        } catch (error) {
            console.error('Failed to load devices:', error);
        }
    }

    /**
     * Handle phone selection change.
     */
    async function onPhoneSelect() {
        const selected = elements.phoneSelect.value;
        if (!selected) {
            setPhone('');
            return;
        }
        setPhone(selected);
        await loadConversation(selected);
    }

    /**
     * Handle adding a new phone number.
     */
    async function onAddPhone() {
        let phone = elements.newPhoneInput.value.trim();
        if (!phone) {
            showToast('Please enter a phone number');
            return;
        }

        phone = normalizePhone(phone);

        setPhone(phone);
        elements.newPhoneInput.value = '';

        let exists = false;
        for (let i = 0; i < elements.phoneSelect.options.length; i++) {
            if (elements.phoneSelect.options[i].value === phone) {
                exists = true;
                elements.phoneSelect.value = phone;
                break;
            }
        }

        if (!exists) {
            const option = document.createElement('option');
            option.value = phone;
            option.textContent = `${phone} (0 msgs)`;
            elements.phoneSelect.appendChild(option);
            elements.phoneSelect.value = phone;
        }

        await loadConversation(phone);
    }

    /**
     * Set the current phone and update UI.
     */
    function setPhone(phone) {
        currentPhone = phone;
        elements.currentPhone.textContent = phone || 'Select a phone to start';

        const enabled = !!phone;
        elements.messageInput.disabled = !enabled;
        elements.sendBtn.disabled = !enabled;

        if (!phone) {
            elements.chatMessages.innerHTML = `
                <div class="welcome-state">
                    <div class="welcome-icon">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                            <rect x="5" y="2" width="14" height="20" rx="2"/>
                            <path d="M12 18h.01"/>
                        </svg>
                    </div>
                    <h3>No Device Selected</h3>
                    <p>Add a phone number above to start simulating SMS conversations</p>
                    <div class="welcome-commands">
                        <code>REG MOTHER CAMP A ZONE 3</code>
                        <code>EMERGENCY</code>
                        <code>HELP</code>
                    </div>
                </div>
            `;
        }
    }

    /**
     * Normalize a phone number.
     */
    function normalizePhone(phone) {
        let normalized = phone.replace(/[\s-]/g, '');

        if (/^\d{10,}$/.test(normalized) && !normalized.startsWith('+')) {
            normalized = '+249' + (normalized.startsWith('0') ? normalized.slice(1) : normalized);
        }

        if (/^249\d+$/.test(normalized)) {
            normalized = '+' + normalized;
        }

        return normalized;
    }

    /**
     * Load conversation for a phone number.
     */
    async function loadConversation(phone) {
        ChatRenderer.showLoading(elements.chatMessages);

        try {
            const conversation = await SimulatorAPI.getConversation(phone);
            ChatRenderer.renderMessages(elements.chatMessages, conversation.messages);
        } catch (error) {
            console.error('Failed to load conversation:', error);
            elements.chatMessages.innerHTML = `
                <div class="error-message">Failed to load conversation: ${error.message}</div>
            `;
        }
    }

    /**
     * Send a message.
     */
    async function onSendMessage() {
        const body = elements.messageInput.value.trim();
        if (!body || !currentPhone) return;

        elements.messageInput.disabled = true;
        elements.sendBtn.disabled = true;

        try {
            const result = await SimulatorAPI.sendMessage(currentPhone, body);

            ChatRenderer.addMessage(elements.chatMessages, result.userMessage);
            ChatRenderer.addMessage(elements.chatMessages, result.systemResponse);

            elements.messageInput.value = '';

            await refreshOutbox();
            await loadDevices();

        } catch (error) {
            console.error('Failed to send message:', error);
            showToast('Failed to send message: ' + error.message);
        } finally {
            elements.messageInput.disabled = false;
            elements.sendBtn.disabled = false;
            elements.messageInput.focus();
        }
    }

    /**
     * Refresh the outbox display.
     */
    async function refreshOutbox() {
        try {
            const outbox = await SimulatorAPI.getOutbox();
            OutboxRenderer.render(elements.outboxMessages, outbox);
            lastOutboxCount = outbox.length;
        } catch (error) {
            console.error('Failed to refresh outbox:', error);
        }
    }

    /**
     * Reset the simulator.
     */
    async function onReset() {
        if (!confirm('Reset all conversations and outbox?')) {
            return;
        }

        try {
            await SimulatorAPI.reset();

            setPhone('');
            elements.phoneSelect.innerHTML = '<option value="">Select a phone...</option>';
            OutboxRenderer.render(elements.outboxMessages, []);
            lastOutboxCount = 0;

        } catch (error) {
            console.error('Failed to reset:', error);
            showToast('Failed to reset: ' + error.message);
        }
    }

    /**
     * Start polling for outbox updates.
     */
    function startPolling() {
        if (pollingInterval) {
            clearInterval(pollingInterval);
        }

        pollingInterval = setInterval(async () => {
            try {
                const outbox = await SimulatorAPI.getOutbox();

                if (outbox.length !== lastOutboxCount) {
                    OutboxRenderer.render(elements.outboxMessages, outbox);
                    lastOutboxCount = outbox.length;

                    if (currentPhone) {
                        await loadConversation(currentPhone);
                    }

                    await loadDevices();
                }
            } catch (error) {
                console.error('Polling error:', error);
            }
        }, 2000);
    }

    /**
     * Stop polling.
     */
    function stopPolling() {
        if (pollingInterval) {
            clearInterval(pollingInterval);
            pollingInterval = null;
        }
    }

    // Initialize when DOM is ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

    window.addEventListener('beforeunload', stopPolling);

})();
