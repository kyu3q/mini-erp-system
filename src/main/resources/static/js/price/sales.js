// 販売単価一覧の初期化
document.addEventListener('DOMContentLoaded', function() {
    initializeDataTable();
});

// DataTablesの初期化
function initializeDataTable() {
    $('#priceTable').DataTable({
        language: {
            url: "https://cdn.datatables.net/plug-ins/1.13.6/i18n/ja.json"
        },
        order: [[4, 'desc']],
        pageLength: 25
    });
}

// 削除確認と実行
function confirmDelete(id) {
    if (confirm('この販売単価を削除してもよろしいですか？')) {
        const form = document.getElementById('deleteForm' + id);
        const methodInput = document.createElement('input');
        methodInput.type = 'hidden';
        methodInput.name = '_method';
        methodInput.value = 'DELETE';
        form.appendChild(methodInput);
        form.submit();
    }
}

// Excel出力
function exportToExcel() {
    // 検索フォームから条件を取得
    const itemCode = document.getElementById('itemCode').value;
    const itemName = document.getElementById('itemName').value;
    const customerCode = document.getElementById('customerCode').value;
    const customerName = document.getElementById('customerName').value;
    const status = document.getElementById('status').value;
    
    // URLパラメータを構築
    let params = new URLSearchParams();
    if (itemCode) params.append('itemCode', itemCode);
    if (itemName) params.append('itemName', itemName);
    if (customerCode) params.append('customerCode', customerCode);
    if (customerName) params.append('customerName', customerName);
    if (status) params.append('status', status);
    
    // Excel出力URLを構築して遷移
    const exportUrl = `/prices/sales/excel/export?${params.toString()}`;
    window.location.href = exportUrl;
}

// テンプレートダウンロード
function downloadTemplate() {
    window.location.href = '/prices/sales/excel/template';
}

// Excel取込
function importFromExcel(input) {
    if (input.files.length === 0) return;

    const formData = new FormData();
    formData.append('file', input.files[0]);

    fetch('/prices/sales/excel/import', {
        method: 'POST',
        body: formData
    })
    .then(response => response.json())
    .then(result => {
        let message = `処理結果:\n`;
        message += `処理件数: ${result.totalCount}件\n`;
        message += `成功件数: ${result.successCount}件\n`;
        message += `失敗件数: ${result.errorCount}件\n\n`;

        if (result.errors.length > 0) {
            message += `エラー内容:\n`;
            result.errors.forEach(error => {
                message += `${error.rowNum}行目: ${error.message}\n`;
            });
        }

        alert(message);
        if (result.successCount > 0) {
            location.reload();
        }
    })
    .catch(error => {
        alert('エラーが発生しました：' + error);
    });

    input.value = '';
}