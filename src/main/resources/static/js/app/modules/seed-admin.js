(function (window, $) {
    var FarmSeedAdminModule = {};
    var state = {
        active: false,
        initialized: false,
        bound: false,
        seedRows: [],
        stageRows: [],
        currentSeedId: 0,
        currentSeedName: "",
        currentStageId: 0,
        seedQualityOptions: [],
        soilOptions: [],
        growthStageOptions: []
    };

    function motion() {
        if (window.FarmUi && $.isFunction(window.FarmUi.motion)) {
            return window.FarmUi.motion();
        }
        if ($.isFunction(window.farmMotion)) {
            return window.farmMotion();
        }
        return {moduleEnterMs: 260, actionFeedbackMs: 1200};
    }

    function asNumber(value, def) {
        if (window.FarmUi && $.isFunction(window.FarmUi.asNumber)) {
            return window.FarmUi.asNumber(value, def);
        }
        var n = Number(value);
        return isNaN(n) ? (def || 0) : n;
    }

    function escapeHtml(text) {
        return $("<div/>").text(text == null ? "" : String(text)).html();
    }

    function showMessage(msg) {
        $.messager.show({
            title: "消息",
            msg: msg || "操作成功",
            timeout: motion().actionFeedbackMs,
            showType: "slide"
        });
    }

    function alertMessage(msg) {
        $.messager.alert("提示", msg || "操作失败");
    }

    function boolOk(res) {
        return window.FarmApi && $.isFunction(FarmApi.isOk) ? FarmApi.isOk(res) : false;
    }

    function listFromPageData(data) {
        if (!data) {
            return [];
        }
        if ($.isArray(data.records)) {
            return data.records;
        }
        if ($.isArray(data.rows)) {
            return data.rows;
        }
        return [];
    }

    function getSeedTypeById(seedTypeId) {
        var targetId = asNumber(seedTypeId, 0);
        var matched = null;
        $.each(state.seedRows, function (_, row) {
            if (asNumber(row.id, 0) === targetId) {
                matched = row;
                return false;
            }
            return true;
        });
        return matched;
    }

    function loadDictionaries(done) {
        var count = 0;
        var target = 3;
        function finish() {
            count += 1;
            if (count >= target && $.isFunction(done)) {
                done();
            }
        }

        FarmApi.listSeedQualityOptions(function (res) {
            state.seedQualityOptions = boolOk(res) && $.isArray(res.data) ? res.data : [];
            $("#seedTypeQualityId").combobox("loadData", state.seedQualityOptions);
            finish();
        }, finish);

        FarmApi.listSoilOptions(function (res) {
            state.soilOptions = boolOk(res) && $.isArray(res.data) ? res.data : [];
            $("#seedTypeSoilIds").combobox("loadData", state.soilOptions);
            finish();
        }, finish);

        FarmApi.listGrowthStageOptions(function (res) {
            state.growthStageOptions = boolOk(res) && $.isArray(res.data) ? res.data : [];
            $("#seedStageGrowthStageId").combobox("loadData", state.growthStageOptions);
            finish();
        }, finish);
    }

    function buildSeedTypePagePayload(param) {
        return {
            name: $("#seedAdminName").textbox("getValue"),
            page: asNumber(param.page, 1),
            rows: asNumber(param.rows, 8),
            sort: param.sort || "id",
            order: param.order || "asc"
        };
    }

    function renderSeedStageManageBtn(index) {
        return "<a href='javascript:void(0)' class='seed-admin-stage-open' data-index='" + index + "'>阶段管理</a>";
    }

    function renderCover(value) {
        var src = $.trim(value || "");
        if (!src) {
            src = farmResolveImg("ui/home/watermark.png");
        }
        return "<img class='seed-admin-cover-thumb' src='" + escapeHtml(src) + "' alt='cover'>";
    }

    function initTypeGrid() {
        $("#seedAdminTypeGrid").datagrid({
            fit: true,
            fitColumns: true,
            striped: true,
            rownumbers: true,
            singleSelect: true,
            pagination: true,
            pageSize: 8,
            pageList: [8, 12, 20],
            idField: "id",
            loader: function (param, success, error) {
                FarmApi.seedTypePage(buildSeedTypePagePayload(param), function (res) {
                    if (!boolOk(res) || !res.data) {
                        success({total: 0, rows: []});
                        return;
                    }
                    var rows = listFromPageData(res.data);
                    state.seedRows = rows;
                    success({
                        total: asNumber(res.data.total, rows.length),
                        rows: rows
                    });
                }, function () {
                    if ($.isFunction(error)) {
                        error.apply(this, arguments);
                    }
                });
            },
            columns: [[
                {field: "id", title: "ID", width: 42, sortable: true},
                {field: "coverImageUrl", title: "封面", width: 72, formatter: function (value) { return renderCover(value); }},
                {field: "name", title: "种子名称", width: 90, sortable: true},
                {field: "seedQualityName", title: "品质", width: 70},
                {field: "level", title: "等级", width: 45, sortable: true},
                {field: "enableSoilTypeNames", title: "可种土壤", width: 120},
                {field: "price", title: "价格", width: 56, sortable: true},
                {field: "fruitPrice", title: "果实单价", width: 72, sortable: true},
                {field: "harvestExperience", title: "收获经验", width: 70},
                {field: "harvestScore", title: "收获积分", width: 70},
                {field: "totalGrowSeconds", title: "总成长(s)", width: 70},
                {
                    field: "action",
                    title: "阶段管理",
                    width: 70,
                    formatter: function (value, row, index) {
                        return renderSeedStageManageBtn(index);
                    }
                }
            ]],
            onSelect: function (index, row) {
                selectSeedType(row);
            },
            onDblClickRow: function () {
                openSeedTypeEditor("edit");
            },
            onLoadSuccess: function () {
                $("#seedAdminPanel .seed-admin-stage-open").off("click").on("click", function () {
                    var index = asNumber($(this).attr("data-index"), -1);
                    if (index < 0) {
                        return;
                    }
                    $("#seedAdminTypeGrid").datagrid("selectRow", index);
                    var row = $("#seedAdminTypeGrid").datagrid("getRows")[index];
                    if (!row) {
                        return;
                    }
                    selectSeedType(row);
                });
            }
        });
    }

    function initStageGrid() {
        $("#seedAdminStageGrid").datagrid({
            fit: true,
            fitColumns: true,
            striped: true,
            rownumbers: true,
            singleSelect: true,
            idField: "id",
            loader: function (param, success, error) {
                if (state.currentSeedId <= 0) {
                    state.stageRows = [];
                    success({total: 0, rows: []});
                    return;
                }
                FarmApi.seedStagePage({seedTypeId: state.currentSeedId}, function (res) {
                    if (!boolOk(res) || !res.data) {
                        state.stageRows = [];
                        success({total: 0, rows: []});
                        return;
                    }
                    var rows = listFromPageData(res.data);
                    state.stageRows = rows;
                    success({
                        total: asNumber(res.data.total, rows.length),
                        rows: rows
                    });
                }, function () {
                    if ($.isFunction(error)) {
                        error.apply(this, arguments);
                    }
                });
            },
            columns: [[
                {field: "id", title: "ID", width: 40},
                {field: "stageIndex", title: "阶段序号", width: 56},
                {field: "growthStageName", title: "阶段标题", width: 84},
                {field: "durationSeconds", title: "成长时长(s)", width: 70},
                {field: "bugProbability", title: "虫害概率", width: 64},
                {field: "width", title: "宽", width: 40},
                {field: "height", title: "高", width: 40},
                {field: "offsetX", title: "offsetX", width: 56},
                {field: "offsetY", title: "offsetY", width: 56},
                {field: "assetUrl", title: "资源URL", width: 180}
            ]],
            onSelect: function (index, row) {
                state.currentStageId = asNumber(row && row.id, 0);
            },
            onDblClickRow: function () {
                openSeedStageEditor("edit");
            }
        });
    }

    function refreshTypeGrid(toFirstPage) {
        if (toFirstPage) {
            $("#seedAdminTypeGrid").datagrid("load", {page: 1});
            return;
        }
        $("#seedAdminTypeGrid").datagrid("reload");
    }

    function refreshStageGrid() {
        $("#seedAdminStageGrid").datagrid("reload");
    }

    function selectSeedType(row) {
        state.currentSeedId = asNumber(row && row.id, 0);
        state.currentSeedName = row && row.name ? row.name : "";
        state.currentStageId = 0;
        if (state.currentSeedId <= 0) {
            $("#seedAdminStageHint").text("请先在上方选择一个种子，再进行阶段管理。");
            refreshStageGrid();
            return;
        }
        $("#seedAdminStageHint").text("当前种子: " + state.currentSeedName + " (ID: " + state.currentSeedId + ")");
        refreshStageGrid();
    }

    function soilIdsByBits(bits) {
        var valueBits = asNumber(bits, 0);
        var ids = [];
        $.each(state.soilOptions, function (_, item) {
            var bit = asNumber(item && item.bitCode, 0);
            if (bit > 0 && (valueBits & bit) === bit) {
                ids.push(String(item.id));
            }
        });
        return ids;
    }

    function previewImageFromUrl(assetUrl) {
        var src = $.trim(assetUrl || "");
        if (!src) {
            $("#seedStagePreviewImage").attr("src", "");
            return;
        }
        $("#seedStagePreviewImage").attr("src", src);
    }

    function fillSeedTypeForm(row) {
        $("#seedTypeEditorForm").form("clear");
        $("#seedTypeEditorForm input[name='id']").val(0);
        $("#seedTypeQualityId").combobox("setValue", "");
        $("#seedTypeSoilIds").combobox("clear");
        if (!row) {
            if (state.seedQualityOptions.length > 0) {
                $("#seedTypeQualityId").combobox("setValue", state.seedQualityOptions[0].id);
            }
            return;
        }

        $("#seedTypeEditorForm").form("load", {
            name: row.name || "",
            level: asNumber(row.level, 1),
            regrowStageIndex: row.regrowStageIndex == null ? "" : row.regrowStageIndex,
            price: asNumber(row.price, 0),
            fruitPrice: asNumber(row.fruitPrice, 0),
            harvestExperience: asNumber(row.harvestExperience, 0),
            harvestScore: asNumber(row.harvestScore, 0),
            harvestFruitNumber: asNumber(row.harvestFruitNumber, 0),
            fruitLossPerBug: asNumber(row.fruitLossPerBug, 0),
            maxBugLimit: asNumber(row.maxBugLimit, 0),
            maxHarvestCount: asNumber(row.maxHarvestCount, 1),
            coverImageUrl: row.coverImageUrl || "",
            description: row.description || ""
        });
        $("#seedTypeEditorForm input[name='id']").val(asNumber(row.id, 0));
        $("#seedTypeQualityId").combobox("setValue", asNumber(row.seedQualityId, 0));
        $("#seedTypeSoilIds").combobox("setValues", soilIdsByBits(row.enableSoilTypeBits));
    }

    function openSeedTypeEditor(mode) {
        if (mode === "edit") {
            var row = $("#seedAdminTypeGrid").datagrid("getSelected");
            if (!row) {
                alertMessage("请先选择要编辑的种子类型");
                return;
            }
            fillSeedTypeForm(row);
            $("#seedTypeEditorDialog").dialog("setTitle", "编辑种子类型").dialog("open");
            return;
        }
        fillSeedTypeForm(null);
        $("#seedTypeEditorDialog").dialog("setTitle", "新增种子类型").dialog("open");
    }

    function saveSeedType() {
        if (!$("#seedTypeEditorForm").form("validate")) {
            alertMessage("请先完善种子必填信息");
            return;
        }
        var id = asNumber($("#seedTypeEditorForm input[name='id']").val(), 0);
        var soilIds = $("#seedTypeSoilIds").combobox("getValues");
        var payload = {
            id: id > 0 ? id : null,
            name: $("#seedTypeEditorForm input[name='name']").textbox("getValue"),
            seedQualityId: asNumber($("#seedTypeQualityId").combobox("getValue"), 0),
            soilTypeIds: (soilIds || []).join(","),
            level: asNumber($("#seedTypeEditorForm input[name='level']").numberbox("getValue"), 1),
            regrowStageIndex: asNumber($("#seedTypeEditorForm input[name='regrowStageIndex']").numberbox("getValue"), 0) || null,
            price: asNumber($("#seedTypeEditorForm input[name='price']").numberbox("getValue"), 0),
            fruitPrice: asNumber($("#seedTypeEditorForm input[name='fruitPrice']").numberbox("getValue"), 0),
            harvestExperience: asNumber($("#seedTypeEditorForm input[name='harvestExperience']").numberbox("getValue"), 0),
            harvestScore: asNumber($("#seedTypeEditorForm input[name='harvestScore']").numberbox("getValue"), 0),
            harvestFruitNumber: asNumber($("#seedTypeEditorForm input[name='harvestFruitNumber']").numberbox("getValue"), 0),
            fruitLossPerBug: asNumber($("#seedTypeEditorForm input[name='fruitLossPerBug']").numberbox("getValue"), 0),
            maxBugLimit: asNumber($("#seedTypeEditorForm input[name='maxBugLimit']").numberbox("getValue"), 0),
            maxHarvestCount: asNumber($("#seedTypeEditorForm input[name='maxHarvestCount']").numberbox("getValue"), 1),
            coverImageUrl: $("#seedTypeCoverImageUrl").textbox("getValue"),
            description: $("#seedTypeEditorForm input[name='description']").textbox("getValue")
        };
        FarmApi.seedTypeSave(payload, function (res) {
            if (!boolOk(res)) {
                alertMessage((res && res.msg) || "保存失败");
                return;
            }
            showMessage((res && res.msg) || "保存成功");
            $("#seedTypeEditorDialog").dialog("close");
            refreshTypeGrid(id <= 0);
        }, function () {
            alertMessage("保存失败，请稍后重试");
        });
    }

    function deleteSeedType() {
        var row = $("#seedAdminTypeGrid").datagrid("getSelected");
        if (!row) {
            alertMessage("请先选择要删除的种子类型");
            return;
        }
        $.messager.confirm("确认", "确认删除种子类型【" + (row.name || "") + "】吗？", function (ok) {
            if (!ok) {
                return;
            }
            FarmApi.seedTypeDelete({id: asNumber(row.id, 0)}, function (res) {
                if (!boolOk(res)) {
                    alertMessage((res && res.msg) || "删除失败");
                    return;
                }
                showMessage((res && res.msg) || "删除成功");
                if (state.currentSeedId === asNumber(row.id, 0)) {
                    state.currentSeedId = 0;
                    state.currentSeedName = "";
                    $("#seedAdminStageHint").text("请先在上方选择一个种子，再进行阶段管理。");
                }
                refreshTypeGrid(false);
                refreshStageGrid();
            }, function () {
                alertMessage("删除失败，请稍后重试");
            });
        });
    }

    function fillSeedStageForm(row) {
        $("#seedStageEditorForm").form("clear");
        $("#seedStageEditorForm input[name='id']").val(0);
        $("#seedStageEditorForm input[name='seedTypeId']").val(state.currentSeedId);
        $("#seedStageEditorForm input[name='width']").numberbox("setValue", 100);
        $("#seedStageEditorForm input[name='height']").numberbox("setValue", 120);
        $("#seedStageEditorForm input[name='offsetX']").numberbox("setValue", 50);
        $("#seedStageEditorForm input[name='offsetY']").numberbox("setValue", 40);
        $("#seedStageEditorForm input[name='durationSeconds']").numberbox("setValue", 30);
        $("#seedStageEditorForm input[name='bugProbability']").numberbox("setValue", 0);
        $("#seedStageEditorForm input[name='stageIndex']").numberbox("setValue", asNumber(state.stageRows.length, 0) + 1);
        previewImageFromUrl("");
        if (!row) {
            return;
        }
        $("#seedStageEditorForm").form("load", {
            stageIndex: asNumber(row.stageIndex, 1),
            growthStageId: asNumber(row.growthStageId, 0),
            durationSeconds: asNumber(row.durationSeconds, 0),
            bugProbability: row.bugProbability == null ? 0 : row.bugProbability,
            width: asNumber(row.width, 100),
            height: asNumber(row.height, 120),
            offsetX: asNumber(row.offsetX, 50),
            offsetY: asNumber(row.offsetY, 40),
            assetUrl: row.assetUrl || ""
        });
        $("#seedStageEditorForm input[name='id']").val(asNumber(row.id, 0));
        $("#seedStageEditorForm input[name='seedTypeId']").val(asNumber(row.seedTypeId, state.currentSeedId));
        previewImageFromUrl(row.assetUrl || "");
    }

    function openSeedStageEditor(mode) {
        if (state.currentSeedId <= 0) {
            alertMessage("请先选择种子类型");
            return;
        }
        if (mode === "edit") {
            var row = $("#seedAdminStageGrid").datagrid("getSelected");
            if (!row) {
                alertMessage("请先选择要编辑的阶段");
                return;
            }
            fillSeedStageForm(row);
            $("#seedStageEditorDialog").dialog("setTitle", "编辑成长阶段").dialog("open");
            return;
        }
        fillSeedStageForm(null);
        $("#seedStageEditorDialog").dialog("setTitle", "新增成长阶段").dialog("open");
    }

    function saveSeedStage() {
        if (state.currentSeedId <= 0) {
            alertMessage("请先选择种子类型");
            return;
        }
        if (!$("#seedStageEditorForm").form("validate")) {
            alertMessage("请先完善阶段信息");
            return;
        }
        var payload = {
            id: asNumber($("#seedStageEditorForm input[name='id']").val(), 0) || null,
            seedTypeId: state.currentSeedId,
            growthStageId: asNumber($("#seedStageGrowthStageId").combobox("getValue"), 0),
            stageIndex: asNumber($("#seedStageEditorForm input[name='stageIndex']").numberbox("getValue"), 1),
            durationSeconds: asNumber($("#seedStageEditorForm input[name='durationSeconds']").numberbox("getValue"), 0),
            bugProbability: Number($("#seedStageEditorForm input[name='bugProbability']").numberbox("getValue") || 0),
            width: asNumber($("#seedStageEditorForm input[name='width']").numberbox("getValue"), 100),
            height: asNumber($("#seedStageEditorForm input[name='height']").numberbox("getValue"), 120),
            offsetX: asNumber($("#seedStageEditorForm input[name='offsetX']").numberbox("getValue"), 50),
            offsetY: asNumber($("#seedStageEditorForm input[name='offsetY']").numberbox("getValue"), 40),
            assetUrl: $("#seedStageAssetUrl").textbox("getValue")
        };
        FarmApi.seedStageSave(payload, function (res) {
            if (!boolOk(res)) {
                alertMessage((res && res.msg) || "阶段保存失败");
                return;
            }
            showMessage((res && res.msg) || "阶段保存成功");
            $("#seedStageEditorDialog").dialog("close");
            refreshStageGrid();
            refreshTypeGrid(false);
        }, function () {
            alertMessage("阶段保存失败，请稍后重试");
        });
    }

    function deleteSeedStage() {
        if (state.currentSeedId <= 0) {
            alertMessage("请先选择种子类型");
            return;
        }
        var row = $("#seedAdminStageGrid").datagrid("getSelected");
        if (!row) {
            alertMessage("请先选择要删除的阶段");
            return;
        }
        $.messager.confirm("确认", "确认删除阶段 #" + asNumber(row.stageIndex, 0) + " 吗？", function (ok) {
            if (!ok) {
                return;
            }
            FarmApi.seedStageDelete({id: asNumber(row.id, 0)}, function (res) {
                if (!boolOk(res)) {
                    alertMessage((res && res.msg) || "删除失败");
                    return;
                }
                showMessage((res && res.msg) || "删除成功");
                refreshStageGrid();
                refreshTypeGrid(false);
            }, function () {
                alertMessage("删除失败，请稍后重试");
            });
        });
    }

    function uploadFile($fileInput, category, onSuccess) {
        var files = $fileInput.prop("files");
        if (!files || files.length <= 0) {
            return;
        }
        var formData = new FormData();
        formData.append("file", files[0]);
        formData.append("category", category);
        $.ajax({
            url: "/file/upload",
            type: "post",
            data: formData,
            processData: false,
            contentType: false,
            dataType: "json",
            success: function (res) {
                if (!boolOk(res) || !res.data) {
                    alertMessage((res && res.msg) || "上传失败");
                    return;
                }
                var url = $.trim(res.data.accessUrl || "");
                if ($.isFunction(onSuccess)) {
                    onSuccess(url, res.data);
                }
                showMessage((res && res.msg) || "上传成功");
            },
            error: function () {
                alertMessage("上传失败，请稍后重试");
            },
            complete: function () {
                $fileInput.val("");
            }
        });
    }

    function openPositionEditor() {
        var assetUrl = $("#seedStageAssetUrl").textbox("getValue");
        if (!$.trim(assetUrl)) {
            alertMessage("请先上传或填写阶段资源图片URL");
            return;
        }
        var width = asNumber($("#seedStageEditorForm input[name='width']").numberbox("getValue"), 100);
        var height = asNumber($("#seedStageEditorForm input[name='height']").numberbox("getValue"), 120);
        var offsetX = asNumber($("#seedStageEditorForm input[name='offsetX']").numberbox("getValue"), 50);
        var offsetY = asNumber($("#seedStageEditorForm input[name='offsetY']").numberbox("getValue"), 40);
        $("#seedStagePositionImage").attr("src", assetUrl).css({
            width: width + "px",
            height: height + "px",
            left: offsetX + "px",
            top: offsetY + "px"
        });
        $("#seedStagePositionImage").draggable({
            containment: "#seedStagePositionCanvas"
        }).resizable({
            handles: "n,e,s,w,ne,se,sw,nw"
        });
        $("#seedStagePositionDialog").dialog("open");
    }

    function applyPositionEditor() {
        var $image = $("#seedStagePositionImage");
        var left = parseInt($image.css("left"), 10) || 0;
        var top = parseInt($image.css("top"), 10) || 0;
        var width = $image.outerWidth() || 0;
        var height = $image.outerHeight() || 0;
        $("#seedStageEditorForm input[name='width']").numberbox("setValue", width);
        $("#seedStageEditorForm input[name='height']").numberbox("setValue", height);
        $("#seedStageEditorForm input[name='offsetX']").numberbox("setValue", left);
        $("#seedStageEditorForm input[name='offsetY']").numberbox("setValue", top);
        $("#seedStagePositionDialog").dialog("close");
        var assetUrl = $("#seedStageAssetUrl").textbox("getValue");
        previewImageFromUrl(assetUrl);
        $("#seedStagePreviewImage").css({
            width: width + "px",
            height: height + "px",
            left: left + "px",
            top: top + "px"
        });
    }

    function bindEvents() {
        $("#seedAdminSearchBtn").on("click", function () {
            refreshTypeGrid(true);
        });
        $("#seedAdminName").textbox("textbox").on("keydown", function (event) {
            if (event.keyCode === 13) {
                refreshTypeGrid(true);
            }
        });
        $("#seedAdminResetBtn").on("click", function () {
            $("#seedAdminName").textbox("setValue", "");
            refreshTypeGrid(true);
        });
        $("#seedAdminAddBtn").on("click", function () {
            openSeedTypeEditor("add");
        });
        $("#seedAdminEditBtn").on("click", function () {
            openSeedTypeEditor("edit");
        });
        $("#seedAdminDeleteBtn").on("click", deleteSeedType);

        $("#seedAdminStageAddBtn").on("click", function () {
            openSeedStageEditor("add");
        });
        $("#seedAdminStageEditBtn").on("click", function () {
            openSeedStageEditor("edit");
        });
        $("#seedAdminStageDeleteBtn").on("click", deleteSeedStage);

        $("#seedTypeSaveBtn").on("click", saveSeedType);
        $("#seedTypeCancelBtn").on("click", function () {
            $("#seedTypeEditorDialog").dialog("close");
        });
        $("#seedStageSaveBtn").on("click", saveSeedStage);
        $("#seedStageCancelBtn").on("click", function () {
            $("#seedStageEditorDialog").dialog("close");
        });

        $("#seedTypeUploadCoverBtn").on("click", function () {
            $("#seedTypeCoverFile").val("");
            $("#seedTypeCoverFile").trigger("click");
        });
        $("#seedTypeCoverFile").on("change", function () {
            uploadFile($("#seedTypeCoverFile"), "seed-cover", function (url) {
                $("#seedTypeCoverImageUrl").textbox("setValue", url);
            });
        });

        $("#seedStageUploadImageBtn").on("click", function () {
            $("#seedStageImageFile").val("");
            $("#seedStageImageFile").trigger("click");
        });
        $("#seedStageImageFile").on("change", function () {
            uploadFile($("#seedStageImageFile"), "seed-stage", function (url) {
                $("#seedStageAssetUrl").textbox("setValue", url);
                previewImageFromUrl(url);
            });
        });

        $("#seedStagePositionEditorBtn").on("click", openPositionEditor);
        $("#seedStagePositionApplyBtn").on("click", applyPositionEditor);
        $("#seedStagePositionCancelBtn").on("click", function () {
            $("#seedStagePositionDialog").dialog("close");
        });

        $("#seedStageAssetUrl").textbox("textbox").on("change", function () {
            var val = $("#seedStageAssetUrl").textbox("getValue");
            previewImageFromUrl(val);
        });
    }

    function initComboboxes() {
        $("#seedTypeQualityId").combobox({
            valueField: "id",
            textField: "text",
            editable: false,
            panelHeight: "auto"
        });
        $("#seedTypeSoilIds").combobox({
            valueField: "id",
            textField: "text",
            editable: false,
            panelHeight: 260,
            multiple: true,
            multivalue: true,
            separator: ","
        });
        $("#seedStageGrowthStageId").combobox({
            valueField: "id",
            textField: "text",
            editable: false,
            panelHeight: "auto"
        });
    }

    function initDialogs() {
        $("#seedTypeEditorDialog").dialog({cls: "seed-admin-dialog-window"});
        $("#seedStageEditorDialog").dialog({cls: "seed-admin-dialog-window"});
        $("#seedStagePositionDialog").dialog({cls: "seed-admin-dialog-window"});
    }

    function init() {
        if (state.initialized) {
            return;
        }
        initComboboxes();
        initTypeGrid();
        initStageGrid();
        initDialogs();
        loadDictionaries(function () {
            refreshTypeGrid(true);
        });
        state.initialized = true;
    }

    function setActive(flag) {
        state.active = !!flag;
        if (!state.bound) {
            bindEvents();
            state.bound = true;
        }
        if (!state.initialized) {
            init();
        }

        if (state.active) {
            if (window.FarmUi && $.isFunction(window.FarmUi.showPanel)) {
                window.FarmUi.showPanel($("#seedAdminPanel"));
            } else {
                $("#seedAdminPanel").stop(true, true).css("display", "none").fadeIn(motion().moduleEnterMs);
            }
            window.setTimeout(function () {
                $("#seedAdminTypeGrid").datagrid("resize");
                $("#seedAdminStageGrid").datagrid("resize");
                refreshTypeGrid(false);
                refreshStageGrid();
            }, motion().moduleEnterMs + 20);
            return;
        }

        $("#seedTypeEditorDialog").dialog("close");
        $("#seedStageEditorDialog").dialog("close");
        $("#seedStagePositionDialog").dialog("close");
        if (window.FarmUi && $.isFunction(window.FarmUi.hidePanel)) {
            window.FarmUi.hidePanel($("#seedAdminPanel"));
        } else {
            $("#seedAdminPanel").stop(true, true).fadeOut(motion().moduleEnterMs);
        }
    }

    FarmSeedAdminModule.setActive = setActive;
    FarmSeedAdminModule.refresh = function () {
        refreshTypeGrid(false);
        refreshStageGrid();
    };

    window.FarmSeedAdminModule = FarmSeedAdminModule;
})(window, window.jQuery);
