function initializeForm() {
    // バリデーションの初期化
    const form = document.getElementById('priceForm');
    form.addEventListener('submit', function(event) {
        // バリデーションチェック
        if (!form.checkValidity()) {
            event.preventDefault();
            event.stopPropagation();
            form.classList.add('was-validated');
        }
    });

    // 日付の初期値設定
    if (!document.getElementById('validFromDate').value) {
        document.getElementById('validFromDate').value = new Date().toISOString().split('T')[0];
    }
    if (!document.getElementById('validToDate').value) {
        const nextYear = new Date();
        nextYear.setFullYear(nextYear.getFullYear() + 1);
        document.getElementById('validToDate').value = nextYear.toISOString().split('T')[0];
    }

    // コード入力時の自動補完
    initializeCodeAutoComplete();
}

function initializeDragAndDrop() {
    const tbody = document.querySelector('#scalesTable tbody');
    new Sortable(tbody, {
        handle: '.drag-handle',
        animation: 150,
        onEnd: function() {
            updateScaleIndexes();
        }
    });
}

function addScale() {
    const tbody = document.querySelector('#scalesTable tbody');
    const template = document.getElementById('scaleRowTemplate');
    const clone = template.content.cloneNode(true);
    const index = tbody.children.length;
    
    clone.querySelector('td:nth-child(2)').textContent = index + 1;
    const inputs = clone.querySelectorAll('input');
    inputs.forEach(input => {
        input.name = input.name.replace('INDEX', index);
    });

    tbody.appendChild(clone);
}

function removeScale(button) {
    const row = button.closest('tr');
    row.remove();
    updateScaleIndexes();
}

function updateScaleIndexes() {
    const tbody = document.querySelector('#scalesTable tbody');
    const rows = tbody.querySelectorAll('tr');
    rows.forEach((row, index) => {
        row.querySelector('td:nth-child(2)').textContent = index + 1;
        const inputs = row.querySelectorAll('input');
        inputs.forEach(input => {
            input.name = input.name.replace(/\d+/, index);
        });
    });
} 