/**
 * Initialize form validation for all forms with data-validate attribute
 */
export function initializeFormValidation() {
    const forms = document.querySelectorAll('form[data-validate]')

    forms.forEach(form => {
        // Remove default browser validation
        form.setAttribute('novalidate', true)

        // Add validation on submit
        form.addEventListener('submit', async (e) => {
            e.preventDefault()

            const isValid = await validateForm(form)
            if (isValid) {
                // If form has data-ajax attribute, handle as AJAX
                if (form.hasAttribute('data-ajax')) {
                    handleAjaxSubmit(form)
                } else {
                    // Otherwise, submit normally
                    form.submit()
                }
            }
        })

        // Add real-time validation on input
        const inputs = form.querySelectorAll('input, textarea, select')
        inputs.forEach(input => {
            input.addEventListener('blur', () => validateField(input))
            input.addEventListener('input', () => {
                // Clear error state when user starts typing
                clearFieldError(input)
            })
        })
    })
}

/**
 * Validate a single form
 * @param {HTMLFormElement} form - The form to validate
 * @returns {Promise<boolean>} Whether the form is valid
 */
async function validateForm(form) {
    let isValid = true
    const inputs = form.querySelectorAll('input, textarea, select')

    for (const input of inputs) {
        const fieldValid = await validateField(input)
        if (!fieldValid) {
            isValid = false
        }
    }

    return isValid
}

/**
 * Validate a single field
 * @param {HTMLInputElement} field - The field to validate
 * @returns {Promise<boolean>} Whether the field is valid
 */
async function validateField(field) {
    const value = field.value.trim()
    const rules = getFieldRules(field)

    // Clear previous errors
    clearFieldError(field)

    // Required validation
    if (rules.required && !value) {
        showFieldError(field, `${getFieldLabel(field)} is required`)
        return false
    }

    // Type-specific validation
    if (value) {
        switch (field.type) {
            case 'email':
                if (!isValidEmail(value)) {
                    showFieldError(field, 'Please enter a valid email address')
                    return false
                }
                break

            case 'number':
                if (!isValidNumber(value, rules)) {
                    showFieldError(field, rules.numberError || 'Please enter a valid number')
                    return false
                }
                break

            case 'date':
                if (!isValidDate(value, rules)) {
                    showFieldError(field, rules.dateError || 'Please enter a valid date')
                    return false
                }
                break
        }

        // Length validation
        if (rules.minLength && value.length < rules.minLength) {
            showFieldError(field, `Minimum length is ${rules.minLength} characters`)
            return false
        }

        if (rules.maxLength && value.length > rules.maxLength) {
            showFieldError(field, `Maximum length is ${rules.maxLength} characters`)
            return false
        }

        // Pattern validation
        if (rules.pattern && !new RegExp(rules.pattern).test(value)) {
            showFieldError(field, rules.patternError || 'Invalid format')
            return false
        }
    }

    // Custom validation function
    if (rules.customValidation) {
        const customResult = await rules.customValidation(value, field)
        if (customResult !== true) {
            showFieldError(field, customResult)
            return false
        }
    }

    // Mark as valid
    field.classList.add('is-valid')
    return true
}

/**
 * Get validation rules for a field
 * @param {HTMLInputElement} field - The field to get rules for
 * @returns {Object} Validation rules
 */
function getFieldRules(field) {
    return {
        required: field.hasAttribute('required'),
        minLength: field.getAttribute('minlength') ? parseInt(field.getAttribute('minlength')) : null,
        maxLength: field.getAttribute('maxlength') ? parseInt(field.getAttribute('maxlength')) : null,
        min: field.getAttribute('min') ? parseFloat(field.getAttribute('min')) : null,
        max: field.getAttribute('max') ? parseFloat(field.getAttribute('max')) : null,
        pattern: field.getAttribute('pattern'),
        patternError: field.getAttribute('data-pattern-error'),
        numberError: field.getAttribute('data-number-error'),
        dateError: field.getAttribute('data-date-error'),
        customValidation: window[field.getAttribute('data-custom-validation')]
    }
}

/**
 * Get field label
 * @param {HTMLInputElement} field - The field to get label for
 * @returns {string} Field label
 */
function getFieldLabel(field) {
    const label = field.closest('.form-group')?.querySelector('label')
    return label ? label.textContent.replace('*', '').trim() : field.name
}

/**
 * Show field error
 * @param {HTMLInputElement} field - The field to show error for
 * @param {string} message - Error message
 */
function showFieldError(field, message) {
    field.classList.add('is-invalid')
    field.classList.remove('is-valid')

    // Create or update error message
    let errorDiv = field.parentElement.querySelector('.invalid-feedback')
    if (!errorDiv) {
        errorDiv = document.createElement('div')
        errorDiv.className = 'invalid-feedback'
        field.parentElement.appendChild(errorDiv)
    }
    errorDiv.textContent = message
}

/**
 * Clear field error
 * @param {HTMLInputElement} field - The field to clear error for
 */
function clearFieldError(field) {
    field.classList.remove('is-invalid', 'is-valid')
    const errorDiv = field.parentElement.querySelector('.invalid-feedback')
    if (errorDiv) {
        errorDiv.remove()
    }
}

/**
 * Email validation
 * @param {string} email - Email to validate
 * @returns {boolean} Whether email is valid
 */
function isValidEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    return emailRegex.test(email)
}

/**
 * Number validation
 * @param {string} value - Value to validate
 * @param {Object} rules - Validation rules
 * @returns {boolean} Whether number is valid
 */
function isValidNumber(value, rules) {
    const num = parseFloat(value)
    if (isNaN(num)) return false
    if (rules.min !== null && num < rules.min) return false
    if (rules.max !== null && num > rules.max) return false
    return true
}

/**
 * Date validation
 * @param {string} value - Value to validate
 * @param {Object} rules - Validation rules
 * @returns {boolean} Whether date is valid
 */
function isValidDate(value, rules) {
    const date = new Date(value)
    if (isNaN(date.getTime())) return false

    const today = new Date()
    today.setHours(0, 0, 0, 0)

    if (rules.min === 'today' && date < today) return false
    if (rules.max === 'today' && date > today) return false

    return true
}

/**
 * Handle AJAX form submission
 * @param {HTMLFormElement} form - The form to submit
 */
async function handleAjaxSubmit(form) {
    const formData = new FormData(form)
    const data = Object.fromEntries(formData.entries())

    try {
        const response = await fetch(form.action, {
            method: form.method || 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify(data)
        })

        if (response.ok) {
            // Trigger custom success event
            form.dispatchEvent(new CustomEvent('ajax-success', { detail: await response.json() }))
        } else {
            // Trigger custom error event
            form.dispatchEvent(new CustomEvent('ajax-error', { detail: response }))
        }
    } catch (error) {
        // Trigger custom error event
        form.dispatchEvent(new CustomEvent('ajax-error', { detail: error }))
    }
}