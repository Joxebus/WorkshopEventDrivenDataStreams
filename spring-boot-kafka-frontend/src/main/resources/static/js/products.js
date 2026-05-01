function filterByCategory(selectElement) {
    var pageSize = selectElement.getAttribute('data-page-size');
    var category = selectElement.value;
    window.location.href = '/products?page=0&size=' + pageSize + '&category=' + category;
}

function changePageSize(selectElement) {
    var category = selectElement.getAttribute('data-category');
    var size = selectElement.value;
    var url = '/products?page=0&size=' + size;
    if (category && category !== 'All') {
        url += '&category=' + category;
    }
    window.location.href = url;
}

function addToCart(button) {
    var productId = button.getAttribute('data-product-id');

    // Disable button and show loading state
    button.disabled = true;
    var originalText = button.textContent;
    button.textContent = 'Adding...';

    // Make AJAX call
    fetch('/products/api/' + productId + '/add-to-cart', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: 'quantity=1'
    })
    .then(function(response) {
        return response.json();
    })
    .then(function(data) {
        if (data.success) {
            showNotification(data.message, 'success');
            // Update cart count if there's a cart indicator
            updateCartCount();
        } else {
            showNotification(data.message, 'error');
        }
    })
    .catch(function(error) {
        showNotification('Error adding product to cart', 'error');
    })
    .finally(function() {
        // Re-enable button and restore original text
        button.disabled = false;
        button.textContent = originalText;
    });
}

function showNotification(message, type) {
    var notification = document.getElementById('notification');
    var messageElement = document.getElementById('notification-message');
    var successIcon = document.getElementById('notification-icon-success');
    var errorIcon = document.getElementById('notification-icon-error');

    // Set message
    messageElement.textContent = message;

    // Show appropriate icon
    if (type === 'success') {
        successIcon.classList.remove('hidden');
        errorIcon.classList.add('hidden');
    } else {
        successIcon.classList.add('hidden');
        errorIcon.classList.remove('hidden');
    }

    // Show notification
    notification.classList.remove('hidden');

    // Auto-hide after 3 seconds
    setTimeout(function() {
        closeNotification();
    }, 3000);
}

function closeNotification() {
    var notification = document.getElementById('notification');
    notification.classList.add('hidden');
}

function updateCartCount() {
    // Fetch current cart count and update UI if cart indicator exists
    fetch('/products/api/cart/count')
        .then(function(response) {
            return response.json();
        })
        .then(function(data) {
            // If there's a cart count element in the layout, update it
            var cartCountElement = document.getElementById('cart-count');
            if (cartCountElement) {
                cartCountElement.textContent = data.count;
            }
        })
        .catch(function(error) {
            console.error('Error updating cart count:', error);
        });
}
