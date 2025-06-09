/**
 * Initialize navigation functionality
 */
export function initializeNavigation() {
    // Highlight active navigation item
    highlightActiveNav()

    // Add smooth scrolling
    initSmoothScrolling()

    // Initialize mobile menu
    initMobileMenu()

    // Add search functionality if search form exists
    initSearchBar()
}

/**
 * Highlight active navigation item based on current URL
 */
function highlightActiveNav() {
    const currentPath = window.location.pathname
    const navLinks = document.querySelectorAll('.navbar-nav .nav-link')

    navLinks.forEach(link => {
        const href = link.getAttribute('href')
        if (href === currentPath || (href !== '/' && currentPath.startsWith(href))) {
            link.classList.add('active')
            // Add icon to active link
            if (!link.querySelector('.bi-check-circle')) {
                const icon = document.createElement('i')
                icon.className = 'bi bi-check-circle ms-1'
                link.appendChild(icon)
            }
        } else {
            link.classList.remove('active')
            // Remove icon from inactive links
            const icon = link.querySelector('.bi-check-circle')
            if (icon) {
                icon.remove()
            }
        }
    })
}

/**
 * Initialize smooth scrolling for anchor links
 */
function initSmoothScrolling() {
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            e.preventDefault()
            const target = document.querySelector(this.getAttribute('href'))
            if (target) {
                target.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                })
            }
        })
    })
}

/**
 * Initialize mobile menu functionality
 */
function initMobileMenu() {
    const toggler = document.querySelector('.navbar-toggler')
    const navCollapse = document.querySelector('.navbar-collapse')

    if (toggler && navCollapse) {
        // Close menu when clicking outside
        document.addEventListener('click', (e) => {
            if (!toggler.contains(e.target) && !navCollapse.contains(e.target)) {
                navCollapse.classList.remove('show')
            }
        })

        // Close menu when clicking on a link
        navCollapse.querySelectorAll('.nav-link').forEach(link => {
            link.addEventListener('click', () => {
                if (window.innerWidth < 768) {
                    navCollapse.classList.remove('show')
                }
            })
        })
    }
}

/**
 * Initialize search bar functionality
 */
function initSearchBar() {
    const searchForm = document.querySelector('.navbar .search-form')
    if (!searchForm) return

    const searchInput = searchForm.querySelector('input[type="search"]')
    const searchButton = searchForm.querySelector('button[type="submit"]')

    if (searchInput && searchButton) {
        // Add search icon to button if not present
        if (!searchButton.querySelector('.bi-search')) {
            searchButton.innerHTML = '<i class="bi bi-search"></i> ' + searchButton.innerHTML
        }

        // Add placeholder with icon
        searchInput.placeholder = '🔍 Search expenses...'

        // Handle search form submission
        searchForm.addEventListener('submit', (e) => {
            e.preventDefault()
            const query = searchInput.value.trim()
            if (query) {
                // Redirect to search results or handle search
                window.location.href = `/expenses?search=${encodeURIComponent(query)}`
            }
        })

        // Add autocomplete functionality
        let searchTimeout
        searchInput.addEventListener('input', (e) => {
            clearTimeout(searchTimeout)
            const query = e.target.value.trim()

            if (query.length >= 2) {
                searchTimeout = setTimeout(() => {
                    showSearchSuggestions(query, searchInput)
                }, 300)
            } else {
                hideSearchSuggestions()
            }
        })
    }
}

/**
 * Show search suggestions
 * @param {string} query - Search query
 * @param {HTMLElement} input - Search input element
 */
async function showSearchSuggestions(query, input) {
    // Create or get suggestions container
    let suggestionsContainer = document.getElementById('search-suggestions')
    if (!suggestionsContainer) {
        suggestionsContainer = document.createElement('div')
        suggestionsContainer.id = 'search-suggestions'
        suggestionsContainer.className = 'search-suggestions position-absolute bg-white border rounded shadow-sm'
        suggestionsContainer.style.top = '100%'
        suggestionsContainer.style.left = '0'
        suggestionsContainer.style.right = '0'
        suggestionsContainer.style.zIndex = '1000'
        input.parentElement.style.position = 'relative'
        input.parentElement.appendChild(suggestionsContainer)
    }

    // Mock suggestions - in real app, fetch from server
    const suggestions = [
        { icon: 'bi-tag', text: `Search for "${query}"` },
        { icon: 'bi-calendar', text: `Expenses containing "${query}"` },
        { icon: 'bi-person', text: `User expenses for "${query}"` }
    ]

    suggestionsContainer.innerHTML = suggestions.map(s => `
        <a href="#" class="d-block px-3 py-2 text-decoration-none text-dark hover-bg-light">
            <i class="bi ${s.icon} me-2"></i>${s.text}
        </a>
    `).join('')

    suggestionsContainer.style.display = 'block'
}

/**
 * Hide search suggestions
 */
function hideSearchSuggestions() {
    const suggestionsContainer = document.getElementById('search-suggestions')
    if (suggestionsContainer) {
        suggestionsContainer.style.display = 'none'
    }
}

/**
 * Add navigation shortcuts
 */
export function addNavigationShortcuts() {
    document.addEventListener('keydown', (e) => {
        // Alt + H: Go to home
        if (e.altKey && e.key === 'h') {
            e.preventDefault()
            window.location.href = '/'
        }

        // Alt + E: Go to expenses
        if (e.altKey && e.key === 'e') {
            e.preventDefault()
            window.location.href = '/expenses'
        }

        // Alt + C: Go to categories
        if (e.altKey && e.key === 'c') {
            e.preventDefault()
            window.location.href = '/categories'
        }

        // Ctrl + K or Cmd + K: Focus search
        if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
            e.preventDefault()
            const searchInput = document.querySelector('.navbar input[type="search"]')
            if (searchInput) {
                searchInput.focus()
            }
        }
    })
}

// Initialize shortcuts
addNavigationShortcuts()