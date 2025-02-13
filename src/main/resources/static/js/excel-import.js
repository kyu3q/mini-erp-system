class ExcelImporter {
    constructor(options) {
        this.options = {
            importButtonId: 'importExcelButton',
            fileInputId: 'fileInput',
            apiEndpoint: '',
            acceptFileTypes: '.xlsx',
            onSuccess: null,
            onError: null,
            ...options
        };

        this.init();
    }

    init() {
        // CSRFトークンの取得
        this.token = $("meta[name='_csrf']").attr("content");
        this.header = $("meta[name='_csrf_header']").attr("content");

        // イベントリスナーの設定
        this.setupEventListeners();
    }

    setupEventListeners() {
        const importButton = $(`#${this.options.importButtonId}`);
        const fileInput = $(`#${this.options.fileInputId}`);

        importButton.on('click', () => fileInput.click());

        fileInput.on('change', (e) => this.handleFileSelect(e));
    }

    handleFileSelect(e) {
        const file = e.target.files[0];
        if (!file) return;

        // ファイル形式の検証
        if (!file.name.toLowerCase().endsWith(this.options.acceptFileTypes)) {
            alert(`Excelファイル（${this.options.acceptFileTypes}）を選択してください。`);
            return;
        }

        this.uploadFile(file);
    }

    async uploadFile(file) {
        const formData = new FormData();
        formData.append('file', file);
        formData.append('_csrf', this.token);

        try {
            const response = await fetch(this.options.apiEndpoint, {
                method: 'POST',
                headers: {
                    [this.header]: this.token
                },
                credentials: 'same-origin',
                body: formData
            });

            if (!response.ok) {
                const text = await response.text();
                throw new Error(text || 'サーバーエラーが発生しました');
            }

            const result = await response.json();
            
            // デフォルトの成功メッセージ
            let message = `処理結果:\n`;
            message += `処理件数: ${result.totalCount}件\n`;
            message += `成功件数: ${result.successCount}件\n`;
            message += `新規作成: ${result.createCount}件\n`;
            message += `更新: ${result.updateCount}件\n`;
            message += `失敗件数: ${result.errorCount}件\n\n`;

            if (result.errors && result.errors.length > 0) {
                message += `エラー内容:\n`;
                result.errors.forEach(error => {
                    message += `${error.rowNum}行目: ${error.message}\n`;
                });
            }

            if (this.options.onSuccess) {
                this.options.onSuccess(result);
            } else {
                alert(message);
                if (result.successCount > 0) {
                    location.reload();
                }
            }
        } catch (error) {
            console.error('Error:', error);
            if (this.options.onError) {
                this.options.onError(error);
            } else {
                alert('エラーが発生しました：' + error.message);
            }
        } finally {
            e.target.value = ''; // ファイル選択をリセット
        }
    }
} 