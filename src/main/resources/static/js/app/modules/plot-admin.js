(function (window, $) {
    var FarmPlotAdminModule = {};
    var Admin = window.FarmAdmin || {};
    var state = {
        active: false,
        inited: false,
        tab: "soil",
        soilQuery: {page: 1, rows: 10, name: ""},
        typeQuery: {page: 1, rows: 10, name: ""},
        userQuery: {page: 1, rows: 10, username: ""},
        currentPolicy: null,
        plotTypeOptions: []
    };

    var DEFAULT_SOIL_COVER = "/oss/defaults/soil/soil-default.png";
    var DEFAULT_PLOT_COVER = "/oss/defaults/plot/plot-cover-default.png";
    var DEFAULT_PLOT_ICON = "/oss/defaults/plot/plot-icon-default.png";

    function asNumber(value, def) {
        if ($.isFunction(Admin.asNumber)) {
            return Admin.asNumber(value, def);
        }
        var n = Number(value);
        return isNaN(n) ? (def || 0) : n;
    }

    function boolOk(res) {
        if ($.isFunction(Admin.boolOk)) {
            return Admin.boolOk(res);
        }
        return window.FarmApi && $.isFunction(window.FarmApi.isOk) && window.FarmApi.isOk(res);
    }

    function trimText(value) {
        if ($.isFunction(Admin.trimText)) {
            return Admin.trimText(value);
        }
        return $.trim(value == null ? "" : String(value));
    }

    function defaultText(value, fallback) {
        var safe = trimText(value);
        return safe.length > 0 ? safe : (fallback || "");
    }

    function normalizePageData(res) {
        if ($.isFunction(Admin.normalizePageResult)) {
            return Admin.normalizePageResult(res);
        }
        var data = (boolOk(res) && res && res.data) ? res.data : {};
        var rows = [];
        if ($.isArray(data.records)) {
            rows = data.records;
        } else if ($.isArray(data.rows)) {
            rows = data.rows;
        } else if ($.isArray(data.list)) {
            rows = data.list;
        }
        return {
            total: asNumber(data.total, rows.length),
            rows: rows
        };
    }

    function escapeHtml(text) {
        return $("<div/>").text(text == null ? "" : String(text)).html();
    }

    function showMessage(msg) {
        if ($.isFunction(Admin.toast)) {
            Admin.toast(msg || "操作成功");
            return;
        }
        $.messager.show({title: "提示", msg: msg || "操作成功", timeout: 1300, showType: "slide"});
    }

    function alertMessage(msg) {
        if ($.isFunction(Admin.alertError)) {
            Admin.alertError(msg || "操作失败");
            return;
        }
        $.messager.alert("提示", msg || "操作失败");
    }

    function setTextboxValue($el, value) {
        if ($.isFunction(Admin.setTextboxValue)) {
            Admin.setTextboxValue($el, value);
            return;
        }
        try {
            $el.textbox("setValue", value == null ? "" : value);
        } catch (ignoreTextboxSet) {
            $el.val(value == null ? "" : value);
        }
    }

    function getTextboxValue($el, fallback) {
        if ($.isFunction(Admin.getTextboxValue)) {
            return Admin.getTextboxValue($el, fallback);
        }
        try {
            return trimText($el.textbox("getValue"));
        } catch (ignoreTextboxGet) {
            return trimText($el.val()) || (fallback || "");
        }
    }

    function setNumberboxValue($el, value) {
        if ($.isFunction(Admin.setNumberboxValue)) {
            Admin.setNumberboxValue($el, value);
            return;
        }
        try {
            $el.numberbox("setValue", value == null ? "" : value);
        } catch (ignoreNumberSet) {
            $el.val(value == null ? "" : value);
        }
    }

    function getNumberboxValue($el, fallback) {
        if ($.isFunction(Admin.getNumberboxValue)) {
            return Admin.getNumberboxValue($el, fallback);
        }
        try {
            return asNumber($el.numberbox("getValue"), fallback || 0);
        } catch (ignoreNumberGet) {
            return asNumber($el.val(), fallback || 0);
        }
    }

    function renderCover(src, fallback) {
        var safe = defaultText(src, fallback);
        return "<img src='" + escapeHtml(safe) + "' alt='' style='width:46px;height:46px;object-fit:cover;border:1px solid rgba(172,236,153,.64);background:rgba(0,62,0,.55)'>";
    }

    function formField($form, name) {
        var key = trimText(name);
        if (!$form || $form.length <= 0 || !key) {
            return $();
        }
        var selector = [
            ".textbox-f[textboxname='" + key + "']",
            ".textbox-f[textboxName='" + key + "']",
            ".numberbox-f[numberboxname='" + key + "']",
            ".numberbox-f[numberboxName='" + key + "']",
            ".combo-f[comboname='" + key + "']",
            ".combo-f[comboName='" + key + "']",
            ".textbox-f[name='" + key + "']",
            ".numberbox-f[name='" + key + "']",
            ".combo-f[name='" + key + "']",
            "input.easyui-textbox[name='" + key + "']",
            "input.easyui-numberbox[name='" + key + "']",
            "input.easyui-combobox[name='" + key + "']",
            "textarea.easyui-textbox[name='" + key + "']"
        ].join(",");
        var $widgetInput = $form.find(selector).first();
        if ($widgetInput.length > 0) {
            return $widgetInput;
        }
        return $form.find("[name='" + key + "']").not(".textbox-value,.spinner-value").first();
    }

    function soilField(name) {
        return formField($("#plotSoilEditorForm"), name);
    }

    function typeField(name) {
        return formField($("#plotTypeEditorForm"), name);
    }

    function policyField(name) {
        return formField($("#plotPolicyEditorForm"), name);
    }

    function userAllocField(name) {
        return formField($("#plotUserAllocationForm"), name);
    }

    function safeResizeGrid(selector) {
        var $grid = $(selector);
        if ($grid.length <= 0) {
            return;
        }
        try {
            $grid.datagrid("resize");
        } catch (ignoreResizeError) {}
    }

    function resizeActiveTabGrid(tabName) {
        var activeTab = tabName || state.tab || "soil";
        if (activeTab === "type") {
            safeResizeGrid("#plotAdminTypeGrid");
            return;
        }
        if (activeTab === "user") {
            safeResizeGrid("#plotAdminUserGrid");
            return;
        }
        safeResizeGrid("#plotAdminSoilGrid");
    }

    function deferResizeActiveGrid(tabName) {
        resizeActiveTabGrid(tabName);
        setTimeout(function () {
            resizeActiveTabGrid(tabName);
        }, 80);
        setTimeout(function () {
            resizeActiveTabGrid(tabName);
        }, 220);
    }

    function switchTab(tabName) {
        state.tab = tabName;
        $(".plot-admin-tab").removeClass("is-active");
        $(".plot-admin-tab[data-tab='" + tabName + "']").addClass("is-active");
        $(".plot-admin-pane").removeClass("is-active");

        if (tabName === "type") {
            $("#plotAdminTypePane").addClass("is-active");
            deferResizeActiveGrid("type");
            refreshTypeGrid();
            return;
        }
        if (tabName === "user") {
            $("#plotAdminUserPane").addClass("is-active");
            deferResizeActiveGrid("user");
            refreshPolicyBoard();
            refreshUserGrid();
            return;
        }

        $("#plotAdminSoilPane").addClass("is-active");
        deferResizeActiveGrid("soil");
        refreshSoilGrid();
    }

    function refreshSoilGrid() {
        $("#plotAdminSoilGrid").datagrid("load", {
            page: state.soilQuery.page,
            rows: state.soilQuery.rows,
            name: state.soilQuery.name
        });
    }

    function refreshTypeGrid() {
        $("#plotAdminTypeGrid").datagrid("load", {
            page: state.typeQuery.page,
            rows: state.typeQuery.rows,
            name: state.typeQuery.name
        });
    }

    function refreshUserGrid() {
        $("#plotAdminUserGrid").datagrid("load", {
            page: state.userQuery.page,
            rows: state.userQuery.rows,
            username: state.userQuery.username
        });
    }

    function refreshPolicyBoard() {
        window.FarmApi.plotPolicyCurrent({}, function (res) {
            if (!boolOk(res) || !res.data) {
                alertMessage((res && res.msg) || "读取策略失败");
                return;
            }
            state.currentPolicy = res.data;
            renderPolicyBoard(res.data);
        }, function () {
            alertMessage("读取策略失败，请稍后重试");
        });
    }

    function renderPolicyBoard(policy) {
        var row = policy || {};
        $("#plotAdminPolicyName").text(defaultText(row.policyName, "-"));
        $("#plotAdminPolicyVersion").text(defaultText(row.policyVersion, "v1"));
        $("#plotAdminPolicyStatus").text(defaultText(row.publishStatus, "DRAFT"));
        $("#plotAdminPolicyScope").text(defaultText(row.effectiveScope, "NEW_USER_ONLY"));
        $("#plotAdminPolicyTotal").text(asNumber(row.defaultTotalPlotCount, 0));
        $("#plotAdminPolicyUnlocked").text(asNumber(row.defaultUnlockedPlotCount, 0));
        $("#plotAdminPolicyLocked").text(asNumber(row.defaultLockedPlotCount, 0));
        $("#plotAdminPolicyDefaultType").text(defaultText(row.defaultPlotTypeName, "-"));
    }

    function loadPlotTypeOptions(callback) {
        window.FarmApi.plotTypePage({page: 1, rows: 500, sort: "sortOrder", order: "asc"}, function (res) {
            var options = [];
            if (boolOk(res) && res.data && $.isArray(res.data.records)) {
                $.each(res.data.records, function (_, item) {
                    options.push({
                        id: asNumber(item.id, 0),
                        text: item.name || ("类型#" + asNumber(item.id, 0))
                    });
                });
            }
            state.plotTypeOptions = options;
            if ($.isFunction(callback)) {
                callback(options);
            }
        }, function () {
            state.plotTypeOptions = [];
            if ($.isFunction(callback)) {
                callback([]);
            }
        });
    }

    function loadSoilOptionsForTypeForm(selectedId) {
        window.FarmApi.plotSoilPage({page: 1, rows: 200, sort: "id", order: "asc"}, function (res) {
            var options = [];
            if (boolOk(res) && res.data && $.isArray(res.data.records)) {
                $.each(res.data.records, function (_, item) {
                    options.push({
                        id: asNumber(item.id, 0),
                        text: item.name || ("土壤#" + asNumber(item.id, 0))
                    });
                });
            }
            $("#plotTypeSoilTypeId").combobox({
                valueField: "id",
                textField: "text",
                editable: false,
                panelHeight: 180,
                data: options
            });
            if (asNumber(selectedId, 0) > 0) {
                $("#plotTypeSoilTypeId").combobox("setValue", asNumber(selectedId, 0));
            } else if (options.length > 0) {
                $("#plotTypeSoilTypeId").combobox("setValue", options[0].id);
            }
        }, function () {
            $("#plotTypeSoilTypeId").combobox({
                valueField: "id",
                textField: "text",
                editable: false,
                panelHeight: 80,
                data: []
            });
        });
    }

    function previewSoilCover(url) {
        $("#plotSoilCoverPreview").attr("src", defaultText(url, DEFAULT_SOIL_COVER));
    }

    function previewTypeCover(url) {
        $("#plotTypeCoverPreview").attr("src", defaultText(url, DEFAULT_PLOT_COVER));
    }

    function openSoilEditor(row) {
        var data = row || {};
        $("#plotSoilEditorForm").form("clear");
        setTextboxValue(soilField("id"), asNumber(data.id, 0));
        setTextboxValue(soilField("name"), data.name || "");
        setTextboxValue($("#plotSoilBitCodeDisplay"), data.id ? String(asNumber(data.bitCode, 0)) : "系统保存时自动分配");
        setNumberboxValue(soilField("level"), asNumber(data.level, 1));
        setNumberboxValue(soilField("unlockExperienceRequired"), asNumber(data.unlockExperienceRequired, 0));
        setTextboxValue(soilField("growSpeedMultiplier"), data.growSpeedMultiplier || "1.00");
        setTextboxValue($("#plotSoilCoverImageUrl"), data.coverImageUrl || DEFAULT_SOIL_COVER);
        setTextboxValue(soilField("description"), data.description || "");
        previewSoilCover(data.coverImageUrl || DEFAULT_SOIL_COVER);
        $("#plotSoilEditorDialog").dialog("setTitle", data.id ? "编辑土壤类型" : "新增土壤类型").dialog("open");
    }

    function openTypeEditor(row) {
        var data = row || {};
        $("#plotTypeEditorForm").form("clear");
        setTextboxValue(typeField("id"), asNumber(data.id, 0));
        setTextboxValue(typeField("name"), data.name || "");
        setTextboxValue(typeField("iconUrl"), data.iconUrl || DEFAULT_PLOT_ICON);
        setTextboxValue($("#plotTypeCoverImageUrl"), data.coverImageUrl || data.iconUrl || DEFAULT_PLOT_COVER);
        setNumberboxValue(typeField("defaultUnlockExperienceRequired"), asNumber(data.defaultUnlockExperienceRequired, 0));
        setNumberboxValue(typeField("sortOrder"), asNumber(data.sortOrder, 0));
        setTextboxValue(typeField("description"), data.description || "");
        previewTypeCover(data.coverImageUrl || data.iconUrl || DEFAULT_PLOT_COVER);
        loadSoilOptionsForTypeForm(data.soilTypeId);
        try {
            (data.unlockRequired === false ? $("#plotTypeUnlockRequired").switchbutton("uncheck") : $("#plotTypeUnlockRequired").switchbutton("check"));
            (data.defaultUsable === false ? $("#plotTypeDefaultUsable").switchbutton("uncheck") : $("#plotTypeDefaultUsable").switchbutton("check"));
        } catch (ignoreSwitchSet) {}
        $("#plotTypeEditorDialog").dialog("setTitle", data.id ? "编辑地块类型" : "新增地块类型").dialog("open");
    }

    function pickText(primary, fallback, def) {
        var p = trimText(primary);
        if (p) {
            return p;
        }
        var f = trimText(fallback);
        if (f) {
            return f;
        }
        return def || "";
    }

    function pickNumber(primary, fallback, def) {
        if (primary !== null && primary !== undefined && primary !== "") {
            return asNumber(primary, def || 0);
        }
        if (fallback !== null && fallback !== undefined && fallback !== "") {
            return asNumber(fallback, def || 0);
        }
        return def || 0;
    }

    function mergeSoilEditorData(detail, row) {
        var d = detail || {};
        var r = row || {};
        return {
            id: pickNumber(d.id, r.id, 0),
            name: pickText(d.name, r.name, ""),
            bitCode: pickNumber(d.bitCode, r.bitCode, 0),
            level: pickNumber(d.level, r.level, 1),
            unlockExperienceRequired: pickNumber(d.unlockExperienceRequired, r.unlockExperienceRequired, 0),
            growSpeedMultiplier: pickText(d.growSpeedMultiplier, r.growSpeedMultiplier, "1.00"),
            coverImageUrl: pickText(d.coverImageUrl, r.coverImageUrl, DEFAULT_SOIL_COVER),
            description: pickText(d.description, r.description, "")
        };
    }

    function mergeTypeEditorData(detail, row) {
        var d = detail || {};
        var r = row || {};
        var iconUrl = pickText(d.iconUrl, r.iconUrl, DEFAULT_PLOT_ICON);
        return {
            id: pickNumber(d.id, r.id, 0),
            name: pickText(d.name, r.name, ""),
            iconUrl: iconUrl,
            coverImageUrl: pickText(d.coverImageUrl, r.coverImageUrl, iconUrl || DEFAULT_PLOT_COVER),
            soilTypeId: pickNumber(d.soilTypeId, r.soilTypeId, 0),
            unlockRequired: (d.unlockRequired === null || d.unlockRequired === undefined) ? r.unlockRequired : d.unlockRequired,
            defaultUsable: (d.defaultUsable === null || d.defaultUsable === undefined) ? r.defaultUsable : d.defaultUsable,
            defaultUnlockExperienceRequired: pickNumber(d.defaultUnlockExperienceRequired, r.defaultUnlockExperienceRequired, 0),
            sortOrder: pickNumber(d.sortOrder, r.sortOrder, 0),
            description: pickText(d.description, r.description, "")
        };
    }

    function loadSoilDetailById(id, done) {
        var targetId = asNumber(id, 0);
        if (targetId <= 0) {
            if ($.isFunction(done)) {
                done(null);
            }
            return;
        }
        window.FarmApi.plotSoilGet({id: targetId}, function (res) {
            if (!boolOk(res) || !res.data) {
                if ($.isFunction(done)) {
                    done(null, (res && res.msg) || "读取土壤详情失败");
                }
                return;
            }
            if ($.isFunction(done)) {
                done(res.data, null);
            }
        }, function () {
            if ($.isFunction(done)) {
                done(null, "读取土壤详情失败，请稍后重试");
            }
        });
    }

    function loadTypeDetailById(id, done) {
        var targetId = asNumber(id, 0);
        if (targetId <= 0) {
            if ($.isFunction(done)) {
                done(null);
            }
            return;
        }
        window.FarmApi.plotTypeGet({id: targetId}, function (res) {
            if (!boolOk(res) || !res.data) {
                if ($.isFunction(done)) {
                    done(null, (res && res.msg) || "读取地块类型详情失败");
                }
                return;
            }
            if ($.isFunction(done)) {
                done(res.data, null);
            }
        }, function () {
            if ($.isFunction(done)) {
                done(null, "读取地块类型详情失败，请稍后重试");
            }
        });
    }

    function openPolicyEditor() {
        var row = state.currentPolicy || {};
        $("#plotPolicyEditorForm").form("clear");
        setTextboxValue(policyField("id"), asNumber(row.id, 0));
        setTextboxValue(policyField("policyName"), defaultText(row.policyName, "default-policy"));
        setTextboxValue(policyField("policyVersion"), defaultText(row.policyVersion, "v1"));
        setNumberboxValue(policyField("defaultTotalPlotCount"), asNumber(row.defaultTotalPlotCount, 6));
        setNumberboxValue(policyField("defaultUnlockedPlotCount"), asNumber(row.defaultUnlockedPlotCount, 1));
        setTextboxValue(policyField("defaultLockRuleCode"), defaultText(row.defaultLockRuleCode, "DEFAULT_LOCKED"));
        setTextboxValue(policyField("defaultLockReason"), defaultText(row.defaultLockReason, "pending unlock"));
        setTextboxValue(policyField("allocationRuleJson"), defaultText(row.allocationRuleJson, "{}"));
        $("#plotPolicyPublishStatus").combobox("setValue", defaultText(row.publishStatus, "DRAFT"));
        $("#plotPolicyEffectiveScope").combobox("setValue", defaultText(row.effectiveScope, "NEW_USER_ONLY"));
        try {
            (row.active ? $("#plotPolicyActiveSwitch").switchbutton("check") : $("#plotPolicyActiveSwitch").switchbutton("uncheck"));
        } catch (ignorePolicySwitch) {}

        loadPlotTypeOptions(function (options) {
            $("#plotPolicyDefaultPlotTypeId").combobox({
                valueField: "id",
                textField: "text",
                editable: false,
                panelHeight: 180,
                data: options
            });
            if (asNumber(row.defaultPlotTypeId, 0) > 0) {
                $("#plotPolicyDefaultPlotTypeId").combobox("setValue", asNumber(row.defaultPlotTypeId, 0));
            } else if (options.length > 0) {
                $("#plotPolicyDefaultPlotTypeId").combobox("setValue", options[0].id);
            }
        });
        $("#plotPolicyEditorDialog").dialog("open");
    }

    function openUserAllocationEditor(row) {
        var data = row || {};
        if (asNumber(data.userId, 0) <= 0) {
            alertMessage("请先选择用户记录");
            return;
        }
        $("#plotUserAllocationForm").form("clear");
        setTextboxValue(userAllocField("id"), asNumber(data.id, 0));
        setTextboxValue(userAllocField("userId"), asNumber(data.userId, 0));
        setTextboxValue($("#plotUserAllocUserName"), (data.username || "-") + " / " + (data.nickname || "-"));
        setTextboxValue($("#plotUserAllocCurrentUnlocked"), asNumber(data.currentUnlockedPlots, 0));
        setNumberboxValue(userAllocField("totalPlotCount"), asNumber(data.totalPlotCount, 1));
        setNumberboxValue(userAllocField("unlockedPlotCount"), asNumber(data.unlockedPlotCount, asNumber(data.currentUnlockedPlots, 0)));
        setTextboxValue(userAllocField("lockRuleCode"), defaultText(data.lockRuleCode, "DEFAULT_LOCKED"));
        setTextboxValue(userAllocField("lockReason"), defaultText(data.lockReason, "pending unlock"));
        setTextboxValue(userAllocField("allocationRuleJson"), defaultText(data.allocationRuleJson, "{}"));
        try {
            (data.active === false ? $("#plotUserAllocActiveSwitch").switchbutton("uncheck") : $("#plotUserAllocActiveSwitch").switchbutton("check"));
        } catch (ignoreUserActiveSwitch) {}

        loadPlotTypeOptions(function (options) {
            $("#plotUserAllocDefaultPlotTypeId").combobox({
                valueField: "id",
                textField: "text",
                editable: false,
                panelHeight: 180,
                data: options
            });
            if (asNumber(data.defaultPlotTypeId, 0) > 0) {
                $("#plotUserAllocDefaultPlotTypeId").combobox("setValue", asNumber(data.defaultPlotTypeId, 0));
            } else if (options.length > 0) {
                $("#plotUserAllocDefaultPlotTypeId").combobox("setValue", options[0].id);
            }
        });
        $("#plotUserAllocationDialog").dialog("open");
    }

    function uploadFile($fileInput, category, onSuccess, onComplete) {
        if ($.isFunction(Admin.uploadFile)) {
            Admin.uploadFile({
                fileInput: $fileInput,
                category: category,
                onSuccess: function (url, payload, raw) {
                    if ($.isFunction(onSuccess)) {
                        onSuccess(url, payload, raw);
                    }
                    showMessage((raw && raw.msg) || "上传成功");
                },
                onError: function (msg) {
                    alertMessage(msg || "上传失败");
                },
                onComplete: onComplete
            });
            return;
        }
        var files = $fileInput.prop("files");
        if (!files || files.length <= 0) {
            if ($.isFunction(onComplete)) {
                onComplete();
            }
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
                var url = trimText(res.data.accessUrl || res.data.path || "");
                if (!url) {
                    var rel = trimText(res.data.relativePath || "");
                    if (rel) {
                        url = "/oss/" + rel.replace(/^\/+/, "");
                    }
                }
                if (!url) {
                    alertMessage("上传成功但未返回访问地址");
                    return;
                }
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
                if ($.isFunction(onComplete)) {
                    onComplete();
                }
            }
        });
    }

    function bindUploadPickerEvents() {
        if ($.isFunction(Admin.bindUploadPicker)) {
            Admin.bindUploadPicker({
                namespace: ".plotAdminUploadSoil",
                buttonSelector: "#plotSoilUploadCoverBtn",
                fileSelector: "#plotSoilCoverFile",
                category: "soil-cover",
                onSuccess: function (url, payload, raw) {
                    setTextboxValue($("#plotSoilCoverImageUrl"), url);
                    previewSoilCover(url);
                    showMessage((raw && raw.msg) || "上传成功");
                },
                onError: function (msg) {
                    alertMessage(msg || "上传失败，请稍后重试");
                }
            });
            Admin.bindUploadPicker({
                namespace: ".plotAdminUploadType",
                buttonSelector: "#plotTypeUploadCoverBtn",
                fileSelector: "#plotTypeCoverFile",
                category: "plot-cover",
                onSuccess: function (url, payload, raw) {
                    setTextboxValue($("#plotTypeCoverImageUrl"), url);
                    previewTypeCover(url);
                    showMessage((raw && raw.msg) || "上传成功");
                },
                onError: function (msg) {
                    alertMessage(msg || "上传失败，请稍后重试");
                }
            });
            return;
        }

        $(document)
            .off("click.plotAdminUpload", "#plotSoilUploadCoverBtn")
            .on("click.plotAdminUpload", "#plotSoilUploadCoverBtn", function () {
                var $file = $("#plotSoilCoverFile");
                var input = $file.get(0);
                if (!input) {
                    return;
                }
                input.value = "";
                input.click();
            });

        $(document)
            .off("click.plotAdminUpload", "#plotTypeUploadCoverBtn")
            .on("click.plotAdminUpload", "#plotTypeUploadCoverBtn", function () {
                var $file = $("#plotTypeCoverFile");
                var input = $file.get(0);
                if (!input) {
                    return;
                }
                input.value = "";
                input.click();
            });

        $(document)
            .off("change.plotAdminUpload", "#plotSoilCoverFile")
            .on("change.plotAdminUpload", "#plotSoilCoverFile", function () {
                uploadFile($(this), "soil-cover", function (url) {
                    setTextboxValue($("#plotSoilCoverImageUrl"), url);
                    previewSoilCover(url);
                });
            });

        $(document)
            .off("change.plotAdminUpload", "#plotTypeCoverFile")
            .on("change.plotAdminUpload", "#plotTypeCoverFile", function () {
                uploadFile($(this), "plot-cover", function (url) {
                    setTextboxValue($("#plotTypeCoverImageUrl"), url);
                    previewTypeCover(url);
                });
            });
    }

    function saveSoil() {
        if (!$("#plotSoilEditorForm").form("validate")) {
            return;
        }
        var payload = {
            id: asNumber(getTextboxValue(soilField("id"), "0"), 0) || null,
            name: getTextboxValue(soilField("name"), ""),
            level: getNumberboxValue(soilField("level"), 1),
            unlockExperienceRequired: getNumberboxValue(soilField("unlockExperienceRequired"), 0),
            growSpeedMultiplier: getTextboxValue(soilField("growSpeedMultiplier"), "1.00"),
            coverImageUrl: getTextboxValue($("#plotSoilCoverImageUrl"), DEFAULT_SOIL_COVER),
            description: getTextboxValue(soilField("description"), "")
        };
        window.FarmApi.plotSoilSave(payload, function (res) {
            if (!boolOk(res)) {
                alertMessage((res && res.msg) || "保存失败");
                return;
            }
            $("#plotSoilEditorDialog").dialog("close");
            showMessage((res && res.msg) || "保存成功");
            refreshSoilGrid();
        }, function () {
            alertMessage("保存失败，请稍后重试");
        });
    }

    function saveType() {
        if (!$("#plotTypeEditorForm").form("validate")) {
            return;
        }
        var unlockRequired = true;
        var defaultUsable = true;
        try {
            unlockRequired = !!$("#plotTypeUnlockRequired").switchbutton("options").checked;
            defaultUsable = !!$("#plotTypeDefaultUsable").switchbutton("options").checked;
        } catch (ignoreSwitchGet) {}

        var payload = {
            id: asNumber(getTextboxValue(typeField("id"), "0"), 0) || null,
            name: getTextboxValue(typeField("name"), ""),
            soilTypeId: asNumber($("#plotTypeSoilTypeId").combobox("getValue"), 0),
            iconUrl: getTextboxValue(typeField("iconUrl"), DEFAULT_PLOT_ICON),
            coverImageUrl: getTextboxValue($("#plotTypeCoverImageUrl"), DEFAULT_PLOT_COVER),
            unlockRequired: unlockRequired,
            defaultUsable: defaultUsable,
            defaultUnlockExperienceRequired: getNumberboxValue(typeField("defaultUnlockExperienceRequired"), 0),
            sortOrder: getNumberboxValue(typeField("sortOrder"), 0),
            description: getTextboxValue(typeField("description"), "")
        };
        if (payload.soilTypeId <= 0) {
            alertMessage("请先选择关联土壤");
            return;
        }
        window.FarmApi.plotTypeSave(payload, function (res) {
            if (!boolOk(res)) {
                alertMessage((res && res.msg) || "保存失败");
                return;
            }
            $("#plotTypeEditorDialog").dialog("close");
            showMessage((res && res.msg) || "保存成功");
            refreshTypeGrid();
        }, function () {
            alertMessage("保存失败，请稍后重试");
        });
    }

    function savePolicy() {
        if (!$("#plotPolicyEditorForm").form("validate")) {
            return;
        }
        var total = getNumberboxValue($("#plotPolicyEditorForm input[name='defaultTotalPlotCount']"), 6);
        var unlocked = getNumberboxValue($("#plotPolicyEditorForm input[name='defaultUnlockedPlotCount']"), 1);
        if (unlocked > total) {
            alertMessage("默认已解锁不能大于总地块数");
            return;
        }
        var active = false;
        try {
            active = !!$("#plotPolicyActiveSwitch").switchbutton("options").checked;
        } catch (ignorePolicySwitchGet) {}

        var payload = {
            id: asNumber(getTextboxValue(policyField("id"), "0"), 0) || null,
            policyName: getTextboxValue(policyField("policyName"), ""),
            policyVersion: getTextboxValue(policyField("policyVersion"), "v1"),
            defaultTotalPlotCount: total,
            defaultUnlockedPlotCount: unlocked,
            defaultLockedPlotCount: Math.max(total - unlocked, 0),
            defaultPlotTypeId: asNumber($("#plotPolicyDefaultPlotTypeId").combobox("getValue"), 0) || null,
            publishStatus: trimText($("#plotPolicyPublishStatus").combobox("getValue")) || "DRAFT",
            effectiveScope: trimText($("#plotPolicyEffectiveScope").combobox("getValue")) || "NEW_USER_ONLY",
            active: active,
            defaultLockRuleCode: getTextboxValue(policyField("defaultLockRuleCode"), "DEFAULT_LOCKED"),
            defaultLockReason: getTextboxValue(policyField("defaultLockReason"), "pending unlock"),
            allocationRuleJson: getTextboxValue(policyField("allocationRuleJson"), "{}")
        };
        window.FarmApi.plotPolicySave(payload, function (res) {
            if (!boolOk(res)) {
                alertMessage((res && res.msg) || "保存策略失败");
                return;
            }
            $("#plotPolicyEditorDialog").dialog("close");
            showMessage((res && res.msg) || "策略保存成功");
            refreshPolicyBoard();
        }, function () {
            alertMessage("保存策略失败，请稍后重试");
        });
    }

    function activatePolicy() {
        var policyId = asNumber(state.currentPolicy && state.currentPolicy.id, 0);
        if (policyId <= 0) {
            alertMessage("当前策略不存在，无法激活");
            return;
        }
        if ($.isFunction(Admin.confirm)) {
            Admin.confirm("确认激活当前策略吗？激活后仅对新用户生效。", doActivatePolicy);
            return;
        }
        $.messager.confirm("确认", "确认激活当前策略吗？激活后仅对新用户生效。", function (ok) {
            if (ok) {
                doActivatePolicy();
            }
        });

        function doActivatePolicy() {
            window.FarmApi.plotPolicyActivate({id: policyId}, function (res) {
                if (!boolOk(res)) {
                    alertMessage((res && res.msg) || "激活策略失败");
                    return;
                }
                showMessage((res && res.msg) || "策略激活成功（仅新用户生效）");
                refreshPolicyBoard();
            }, function () {
                alertMessage("激活策略失败，请稍后重试");
            });
        }
    }

    function saveUserAllocation() {
        if (!$("#plotUserAllocationForm").form("validate")) {
            return;
        }
        var userId = asNumber(getTextboxValue(userAllocField("userId"), "0"), 0);
        var total = getNumberboxValue(userAllocField("totalPlotCount"), 1);
        var unlocked = getNumberboxValue(userAllocField("unlockedPlotCount"), 0);
        var currentUnlocked = asNumber(getTextboxValue($("#plotUserAllocCurrentUnlocked"), "0"), 0);
        if (unlocked > total) {
            alertMessage("配置已解锁不能大于配置总地块");
            return;
        }
        if (unlocked < currentUnlocked) {
            alertMessage("不允许回滚已解锁地块进度");
            return;
        }
        var active = true;
        try {
            active = !!$("#plotUserAllocActiveSwitch").switchbutton("options").checked;
        } catch (ignoreUserActiveGet) {}

        var payload = {
            id: asNumber(getTextboxValue(userAllocField("id"), "0"), 0) || null,
            userId: userId,
            totalPlotCount: total,
            unlockedPlotCount: unlocked,
            defaultPlotTypeId: asNumber($("#plotUserAllocDefaultPlotTypeId").combobox("getValue"), 0) || null,
            active: active,
            lockRuleCode: getTextboxValue(userAllocField("lockRuleCode"), "DEFAULT_LOCKED"),
            lockReason: getTextboxValue(userAllocField("lockReason"), "pending unlock"),
            allocationRuleJson: getTextboxValue(userAllocField("allocationRuleJson"), "{}")
        };
        window.FarmApi.plotUserUpdate(payload, function (res) {
            if (!boolOk(res)) {
                alertMessage((res && res.msg) || "保存用户配置失败");
                return;
            }
            $("#plotUserAllocationDialog").dialog("close");
            showMessage((res && res.msg) || "用户配置保存成功");
            refreshUserGrid();
        }, function () {
            alertMessage("保存用户配置失败，请稍后重试");
        });
    }

    function deleteSoil() {
        var row = $("#plotAdminSoilGrid").datagrid("getSelected");
        if (!row) {
            alertMessage("请先选择要删除的土壤类型");
            return;
        }
        if ($.isFunction(Admin.confirm)) {
            Admin.confirm("确认删除土壤类型【" + escapeHtml(row.name || "") + "】吗？", doDeleteSoil);
            return;
        }
        $.messager.confirm("确认", "确认删除土壤类型【" + escapeHtml(row.name || "") + "】吗？", function (ok) {
            if (ok) {
                doDeleteSoil();
            }
        });

        function doDeleteSoil() {
            window.FarmApi.plotSoilDelete({id: asNumber(row.id, 0)}, function (res) {
                if (!boolOk(res)) {
                    alertMessage((res && res.msg) || "删除失败");
                    return;
                }
                showMessage((res && res.msg) || "删除成功");
                refreshSoilGrid();
            }, function () {
                alertMessage("删除失败，请稍后重试");
            });
        }
    }

    function deleteType() {
        var row = $("#plotAdminTypeGrid").datagrid("getSelected");
        if (!row) {
            alertMessage("请先选择要删除的地块类型");
            return;
        }
        if ($.isFunction(Admin.confirm)) {
            Admin.confirm("确认删除地块类型【" + escapeHtml(row.name || "") + "】吗？", doDeleteType);
            return;
        }
        $.messager.confirm("确认", "确认删除地块类型【" + escapeHtml(row.name || "") + "】吗？", function (ok) {
            if (ok) {
                doDeleteType();
            }
        });

        function doDeleteType() {
            window.FarmApi.plotTypeDelete({id: asNumber(row.id, 0)}, function (res) {
                if (!boolOk(res)) {
                    alertMessage((res && res.msg) || "删除失败");
                    return;
                }
                showMessage((res && res.msg) || "删除成功");
                refreshTypeGrid();
            }, function () {
                alertMessage("删除失败，请稍后重试");
            });
        }
    }

    function bindEvents() {
        $(".plot-admin-tab").off("click.plotAdminTab").on("click.plotAdminTab", function () {
            switchTab($(this).attr("data-tab") || "soil");
        });

        $("#plotAdminSoilSearchBtn").off("click.plotAdmin").on("click.plotAdmin", function () {
            state.soilQuery.name = getTextboxValue($("#plotAdminSoilName"), "");
            state.soilQuery.page = 1;
            refreshSoilGrid();
        });
        $("#plotAdminSoilResetBtn").off("click.plotAdmin").on("click.plotAdmin", function () {
            setTextboxValue($("#plotAdminSoilName"), "");
            state.soilQuery.name = "";
            state.soilQuery.page = 1;
            refreshSoilGrid();
        });
        $("#plotAdminSoilAddBtn").off("click.plotAdmin").on("click.plotAdmin", function () {
            openSoilEditor(null);
        });
        $("#plotAdminSoilEditBtn").off("click.plotAdmin").on("click.plotAdmin", function () {
            var row = $("#plotAdminSoilGrid").datagrid("getSelected");
            if (!row) {
                alertMessage("请先选择要编辑的土壤类型");
                return;
            }
            loadSoilDetailById(row.id, function (detail, errMsg) {
                if (detail) {
                    openSoilEditor(mergeSoilEditorData(detail, row));
                    return;
                }
                if (errMsg) {
                    alertMessage(errMsg + "，已回退使用列表数据");
                }
                openSoilEditor(row);
            });
        });
        $("#plotAdminSoilDeleteBtn").off("click.plotAdmin").on("click.plotAdmin", function () {
            deleteSoil();
        });

        $("#plotAdminTypeSearchBtn").off("click.plotAdmin").on("click.plotAdmin", function () {
            state.typeQuery.name = getTextboxValue($("#plotAdminTypeName"), "");
            state.typeQuery.page = 1;
            refreshTypeGrid();
        });
        $("#plotAdminTypeResetBtn").off("click.plotAdmin").on("click.plotAdmin", function () {
            setTextboxValue($("#plotAdminTypeName"), "");
            state.typeQuery.name = "";
            state.typeQuery.page = 1;
            refreshTypeGrid();
        });
        $("#plotAdminTypeAddBtn").off("click.plotAdmin").on("click.plotAdmin", function () {
            openTypeEditor(null);
        });
        $("#plotAdminTypeEditBtn").off("click.plotAdmin").on("click.plotAdmin", function () {
            var row = $("#plotAdminTypeGrid").datagrid("getSelected");
            if (!row) {
                alertMessage("请先选择要编辑的地块类型");
                return;
            }
            loadTypeDetailById(row.id, function (detail, errMsg) {
                if (detail) {
                    openTypeEditor(mergeTypeEditorData(detail, row));
                    return;
                }
                if (errMsg) {
                    alertMessage(errMsg + "，已回退使用列表数据");
                }
                openTypeEditor(row);
            });
        });
        $("#plotAdminTypeDeleteBtn").off("click.plotAdmin").on("click.plotAdmin", function () {
            deleteType();
        });

        $("#plotAdminUserSearchBtn").off("click.plotAdmin").on("click.plotAdmin", function () {
            state.userQuery.username = getTextboxValue($("#plotAdminUserName"), "");
            state.userQuery.page = 1;
            refreshUserGrid();
        });
        $("#plotAdminUserResetBtn").off("click.plotAdmin").on("click.plotAdmin", function () {
            setTextboxValue($("#plotAdminUserName"), "");
            state.userQuery.username = "";
            state.userQuery.page = 1;
            refreshUserGrid();
        });
        $("#plotAdminUserEditBtn").off("click.plotAdmin").on("click.plotAdmin", function () {
            var row = $("#plotAdminUserGrid").datagrid("getSelected");
            if (!row) {
                alertMessage("请先选择用户记录");
                return;
            }
            openUserAllocationEditor(row);
        });

        $("#plotAdminPolicyRefreshBtn").off("click.plotAdmin").on("click.plotAdmin", function () {
            refreshPolicyBoard();
        });
        $("#plotAdminPolicyEditBtn").off("click.plotAdmin").on("click.plotAdmin", function () {
            openPolicyEditor();
        });
        $("#plotAdminPolicyActivateBtn").off("click.plotAdmin").on("click.plotAdmin", function () {
            activatePolicy();
        });

        $("#plotSoilSaveBtn").off("click.plotAdmin").on("click.plotAdmin", function () {
            saveSoil();
        });
        $("#plotSoilCancelBtn").off("click.plotAdmin").on("click.plotAdmin", function () {
            $("#plotSoilEditorDialog").dialog("close");
        });
        $("#plotTypeSaveBtn").off("click.plotAdmin").on("click.plotAdmin", function () {
            saveType();
        });
        $("#plotTypeCancelBtn").off("click.plotAdmin").on("click.plotAdmin", function () {
            $("#plotTypeEditorDialog").dialog("close");
        });
        $("#plotPolicySaveBtn").off("click.plotAdmin").on("click.plotAdmin", function () {
            savePolicy();
        });
        $("#plotPolicyCancelBtn").off("click.plotAdmin").on("click.plotAdmin", function () {
            $("#plotPolicyEditorDialog").dialog("close");
        });
        $("#plotUserAllocSaveBtn").off("click.plotAdmin").on("click.plotAdmin", function () {
            saveUserAllocation();
        });
        $("#plotUserAllocCancelBtn").off("click.plotAdmin").on("click.plotAdmin", function () {
            $("#plotUserAllocationDialog").dialog("close");
        });

        bindUploadPickerEvents();
        $("#plotSoilCoverImageUrl").textbox("textbox").off("change.plotAdmin blur.plotAdmin input.plotAdmin")
            .on("change.plotAdmin blur.plotAdmin input.plotAdmin", function () {
                previewSoilCover(getTextboxValue($("#plotSoilCoverImageUrl"), DEFAULT_SOIL_COVER));
            });
        $("#plotTypeCoverImageUrl").textbox("textbox").off("change.plotAdmin blur.plotAdmin input.plotAdmin")
            .on("change.plotAdmin blur.plotAdmin input.plotAdmin", function () {
                previewTypeCover(getTextboxValue($("#plotTypeCoverImageUrl"), DEFAULT_PLOT_COVER));
            });
    }

    function initSoilGrid() {
        $("#plotAdminSoilGrid").datagrid({
            fit: true,
            border: false,
            fitColumns: true,
            striped: true,
            nowrap: false,
            singleSelect: true,
            pagination: true,
            rownumbers: true,
            loader: function (param, success, error) {
                window.FarmApi.plotSoilPage({
                    page: asNumber(param.page, state.soilQuery.page),
                    rows: asNumber(param.rows, state.soilQuery.rows),
                    name: trimText(param.name)
                }, function (res) {
                    success(normalizePageData(res));
                }, function () {
                    if ($.isFunction(error)) {
                        error.apply(this, arguments);
                    }
                });
            },
            queryParams: {
                page: state.soilQuery.page,
                rows: state.soilQuery.rows,
                name: state.soilQuery.name
            },
            columns: [[
                {field: "id", title: "ID", width: 52, align: "center"},
                {field: "name", title: "土壤名称", width: 110},
                {field: "bitCode", title: "bitCode", width: 72, align: "center"},
                {field: "level", title: "等级", width: 56, align: "center"},
                {field: "unlockExperienceRequired", title: "解锁经验", width: 86, align: "right"},
                {field: "growSpeedMultiplier", title: "成长倍率", width: 80, align: "center"},
                {field: "coverImageUrl", title: "图片", width: 66, align: "center", formatter: function (v) { return renderCover(v, DEFAULT_SOIL_COVER); }},
                {field: "description", title: "描述", width: 250}
            ]],
            onLoadSuccess: function () {
                var pager = $("#plotAdminSoilGrid").datagrid("getPager");
                pager.pagination({
                    onSelectPage: function (pageNo, pageSize) {
                        state.soilQuery.page = pageNo;
                        state.soilQuery.rows = pageSize;
                        refreshSoilGrid();
                    }
                });
            }
        });
    }

    function initTypeGrid() {
        $("#plotAdminTypeGrid").datagrid({
            fit: true,
            border: false,
            fitColumns: true,
            striped: true,
            nowrap: false,
            singleSelect: true,
            pagination: true,
            rownumbers: true,
            loader: function (param, success, error) {
                window.FarmApi.plotTypePage({
                    page: asNumber(param.page, state.typeQuery.page),
                    rows: asNumber(param.rows, state.typeQuery.rows),
                    name: trimText(param.name)
                }, function (res) {
                    success(normalizePageData(res));
                }, function () {
                    if ($.isFunction(error)) {
                        error.apply(this, arguments);
                    }
                });
            },
            queryParams: {
                page: state.typeQuery.page,
                rows: state.typeQuery.rows,
                name: state.typeQuery.name
            },
            columns: [[
                {field: "id", title: "ID", width: 52, align: "center"},
                {field: "name", title: "地块类型", width: 110},
                {field: "soilTypeName", title: "关联土壤", width: 98},
                {field: "unlockRequired", title: "需解锁", width: 66, align: "center", formatter: function (v) { return v ? "是" : "否"; }},
                {field: "defaultUsable", title: "默认可用", width: 74, align: "center", formatter: function (v) { return v ? "是" : "否"; }},
                {field: "defaultUnlockExperienceRequired", title: "默认经验", width: 84, align: "right"},
                {field: "sortOrder", title: "排序", width: 56, align: "center"},
                {field: "coverImageUrl", title: "封面", width: 66, align: "center", formatter: function (v) { return renderCover(v, DEFAULT_PLOT_COVER); }},
                {field: "description", title: "描述", width: 225}
            ]],
            onLoadSuccess: function () {
                var pager = $("#plotAdminTypeGrid").datagrid("getPager");
                pager.pagination({
                    onSelectPage: function (pageNo, pageSize) {
                        state.typeQuery.page = pageNo;
                        state.typeQuery.rows = pageSize;
                        refreshTypeGrid();
                    }
                });
            }
        });
    }

    function initUserGrid() {
        $("#plotAdminUserGrid").datagrid({
            fit: true,
            border: false,
            fitColumns: true,
            striped: true,
            nowrap: false,
            singleSelect: true,
            pagination: true,
            rownumbers: true,
            loader: function (param, success, error) {
                window.FarmApi.plotUserPage({
                    page: asNumber(param.page, state.userQuery.page),
                    rows: asNumber(param.rows, state.userQuery.rows),
                    username: trimText(param.username)
                }, function (res) {
                    success(normalizePageData(res));
                }, function () {
                    if ($.isFunction(error)) {
                        error.apply(this, arguments);
                    }
                });
            },
            queryParams: {
                page: state.userQuery.page,
                rows: state.userQuery.rows,
                username: state.userQuery.username
            },
            columns: [[
                {field: "userId", title: "用户ID", width: 76, align: "center"},
                {field: "username", title: "用户名", width: 110},
                {field: "nickname", title: "昵称", width: 110},
                {field: "currentTotalPlots", title: "当前总地块", width: 92, align: "center"},
                {field: "currentUnlockedPlots", title: "当前已解锁", width: 92, align: "center"},
                {field: "totalPlotCount", title: "配置总地块", width: 92, align: "center"},
                {field: "unlockedPlotCount", title: "配置已解锁", width: 96, align: "center"},
                {field: "defaultPlotTypeName", title: "默认地块类型", width: 112}
            ]],
            onDblClickRow: function (index, row) {
                openUserAllocationEditor(row);
            },
            onLoadSuccess: function () {
                var pager = $("#plotAdminUserGrid").datagrid("getPager");
                pager.pagination({
                    onSelectPage: function (pageNo, pageSize) {
                        state.userQuery.page = pageNo;
                        state.userQuery.rows = pageSize;
                        refreshUserGrid();
                    }
                });
            }
        });
    }

    function initDialogs() {
        $("#plotSoilEditorDialog, #plotTypeEditorDialog, #plotPolicyEditorDialog, #plotUserAllocationDialog").dialog({
            cls: "farm-dialog-window plot-admin-dialog-window"
        });
        $("#plotTypeUnlockRequired").switchbutton();
        $("#plotTypeDefaultUsable").switchbutton();
        $("#plotPolicyActiveSwitch").switchbutton();
        $("#plotUserAllocActiveSwitch").switchbutton();

        $("#plotPolicyPublishStatus").combobox({
            valueField: "id",
            textField: "text",
            editable: false,
            panelHeight: 120,
            data: [
                {id: "DRAFT", text: "DRAFT"},
                {id: "ACTIVE", text: "ACTIVE"},
                {id: "ARCHIVED", text: "ARCHIVED"}
            ]
        });
        $("#plotPolicyEffectiveScope").combobox({
            valueField: "id",
            textField: "text",
            editable: false,
            panelHeight: 100,
            data: [
                {id: "NEW_USER_ONLY", text: "NEW_USER_ONLY"},
                {id: "MANUAL_APPLY", text: "MANUAL_APPLY"}
            ]
        });
    }

    function ensureInit() {
        if (state.inited) {
            return;
        }
        $("#plotAdminSoilName, #plotAdminTypeName, #plotAdminUserName, #plotUserAllocUserName, #plotUserAllocCurrentUnlocked, #plotSoilBitCodeDisplay").textbox();
        $(".plot-admin-toolbar .easyui-linkbutton, .plot-admin-dialog-actions .easyui-linkbutton").linkbutton();
        initSoilGrid();
        initTypeGrid();
        initUserGrid();
        initDialogs();
        bindEvents();
        state.inited = true;
    }

    function setActive(flag) {
        state.active = !!flag;
        if (state.active) {
            ensureInit();
            if (window.FarmUi && $.isFunction(window.FarmUi.showPanel)) {
                window.FarmUi.showPanel($("#plotAdminPanel"));
            } else {
                $("#plotAdminPanel").stop(true, true).css("display", "none").fadeIn(window.farmMotion().moduleEnterMs);
            }
            switchTab(state.tab || "soil");
            setTimeout(function () {
                deferResizeActiveGrid(state.tab || "soil");
            }, 120);
            return;
        }
        if (window.FarmUi && $.isFunction(window.FarmUi.hidePanel)) {
            window.FarmUi.hidePanel($("#plotAdminPanel"));
        } else {
            $("#plotAdminPanel").stop(true, true).fadeOut(window.farmMotion().moduleEnterMs);
        }
    }

    FarmPlotAdminModule.setActive = setActive;
    FarmPlotAdminModule.reload = function () {
        if (state.active) {
            switchTab(state.tab || "soil");
        }
    };
    window.FarmPlotAdminModule = FarmPlotAdminModule;
    if (window.FarmCore && $.isFunction(window.FarmCore.registerSetActiveModule)) {
        window.FarmCore.registerSetActiveModule("plot-admin", FarmPlotAdminModule, {refreshMethod: "reload"});
    }
})(window, window.jQuery);
