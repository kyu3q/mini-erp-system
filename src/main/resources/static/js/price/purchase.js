// 購買単価一覧の初期化
document.addEventListener('DOMContentLoaded', function() {
    initializeDataTable();
});

// DataTablesの初期化
function initializeDataTable() {
    $('#priceTable').DataTable({
        language: {
            url: "https://cdn.datatables.net/plug-ins/1.13.6/i18n/ja.json"
        },
        order: [[5, 'desc']],
        pageLength: 25
    });
}

// 削除確認と実行
function confirmDelete(id) {
    if (confirm('この購買単価を削除してもよろしいですか？')) {
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
    window.location.href = '/prices/excel/purchase/export';
}

// テンプレートダウンロード
function downloadTemplate() {
    window.location.href = '/prices/excel/purchase/template';
}

// Excel取込
function importFromExcel(input) {
    if (input.files.length === 0) return;

    const formData = new FormData();
    formData.append('file', input.files[0]);

    fetch('/prices/excel/purchase/import', {
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