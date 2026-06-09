(function (window, $) {
    var FarmAudio = {};
    var cache = {};
    var state = {
        effectEnabled: true,
        effectVolume: 0.8,
        bgmEnabled: true,
        bgmVolume: 0.6,
        bgmUrl: "",
        bgmEffectiveUrl: "",
        bgmAudio: null
    };
    var STORAGE_KEY = "farm_audio_settings_v1";
    var keyToPath = {
        click: "sfx/farm/ui-seed-selector-open.wav",
        open: "sfx/farm/ui-seed-selector-open.wav",
        plant: "sfx/farm/seed-place-ground.wav",
        harvest: "sfx/farm/crop-harvest.wav",
        care: "sfx/farm/crop-bug-remove.wav",
        clean: "sfx/farm/crop-clean-dead-leaf.wav",
        error: "sfx/farm/error-taunt.wav",
        buy: "sfx/farm/seed-place-ground.wav",
        sell: "sfx/farm/crop-harvest.wav"
    };

    function resolveUrl(key) {
        var relativePath = keyToPath[key] || "";
        if (!relativePath) {
            return "";
        }
        return farmResolveSound(relativePath);
    }

    function clampVolume(value, def) {
        var num = Number(value);
        if (isNaN(num)) {
            num = def;
        }
        if (num < 0) {
            num = 0;
        }
        if (num > 1) {
            num = 1;
        }
        return num;
    }

    function volumeText(value) {
        return Math.round(clampVolume(value, 0) * 100) + "%";
    }

    function safeCreateAudio(url) {
        if (!url || !window.Audio) {
            return null;
        }
        var audio = new Audio(url);
        audio.preload = "auto";
        return audio;
    }

    function resolveDefaultBgmUrl() {
        if (!window.farmDefaultAsset || !$.isFunction(window.farmDefaultAsset)) {
            return "";
        }
        return $.trim(window.farmDefaultAsset("bgm") || "");
    }

    function resolveEffectiveBgmUrl() {
        return $.trim(state.bgmUrl || "") || resolveDefaultBgmUrl();
    }

    function ensureAudio(key) {
        if (cache[key]) {
            return cache[key];
        }
        var audio = safeCreateAudio(resolveUrl(key));
        if (audio) {
            cache[key] = audio;
        }
        return audio;
    }

    function ensureBgmAudio() {
        var effectiveUrl = resolveEffectiveBgmUrl();
        if (!effectiveUrl) {
            state.bgmEffectiveUrl = "";
            if (state.bgmAudio) {
                state.bgmAudio.pause();
                state.bgmAudio = null;
            }
            return null;
        }
        if (state.bgmAudio && state.bgmEffectiveUrl !== effectiveUrl) {
            state.bgmAudio.pause();
            state.bgmAudio = null;
        }
        if (!state.bgmAudio) {
            var audio = safeCreateAudio(effectiveUrl);
            if (!audio) {
                return null;
            }
            audio.loop = true;
            audio.volume = state.bgmVolume;
            state.bgmAudio = audio;
            state.bgmEffectiveUrl = effectiveUrl;
        }
        return state.bgmAudio;
    }

    function syncBgm() {
        var bgm = ensureBgmAudio();
        if (!bgm) {
            return;
        }
        bgm.volume = state.bgmVolume;
        if (state.bgmEnabled) {
            var promise = bgm.play();
            if (promise && $.isFunction(promise.catch)) {
                promise.catch(function () {
                });
            }
            return;
        }
        bgm.pause();
        try {
            bgm.currentTime = 0;
        } catch (e) {
        }
    }

    function saveLocalSettings() {
        if (!window.localStorage) {
            return;
        }
        try {
            window.localStorage.setItem(STORAGE_KEY, JSON.stringify({
                effectEnabled: state.effectEnabled,
                effectVolume: state.effectVolume,
                bgmEnabled: state.bgmEnabled,
                bgmVolume: state.bgmVolume
            }));
        } catch (e) {
        }
    }

    function loadLocalSettings() {
        if (!window.localStorage) {
            return;
        }
        try {
            var raw = window.localStorage.getItem(STORAGE_KEY);
            if (!raw) {
                return;
            }
            var obj = JSON.parse(raw);
            state.effectEnabled = !(obj && obj.effectEnabled === false);
            state.effectVolume = clampVolume(obj && obj.effectVolume, state.effectVolume);
            state.bgmEnabled = !(obj && obj.bgmEnabled === false);
            state.bgmVolume = clampVolume(obj && obj.bgmVolume, state.bgmVolume);
        } catch (e) {
        }
    }

    function updateSettingsText() {
        $("#settingEffectText").text(state.effectEnabled ? "开启" : "关闭");
        $("#settingBgmText").text(state.bgmEnabled ? "开启" : "关闭");
        $("#settingEffectVolumeText").text(volumeText(state.effectVolume));
        $("#settingBgmVolumeText").text(volumeText(state.bgmVolume));
    }

    function renderSettings() {
        $("#settingEffectEnable").prop("checked", state.effectEnabled);
        $("#settingBgmEnable").prop("checked", state.bgmEnabled);
        $("#settingEffectVolume").val(state.effectVolume);
        $("#settingBgmVolume").val(state.bgmVolume);
        updateSettingsText();
    }

    function readSettingsForm() {
        state.effectEnabled = $("#settingEffectEnable").prop("checked") === true;
        state.bgmEnabled = $("#settingBgmEnable").prop("checked") === true;
        state.effectVolume = clampVolume($("#settingEffectVolume").val(), state.effectVolume);
        state.bgmVolume = clampVolume($("#settingBgmVolume").val(), state.bgmVolume);
    }

    function exportSettingsPayload() {
        return {
            effectEnabled: state.effectEnabled,
            effectVolume: state.effectVolume,
            bgmEnabled: state.bgmEnabled,
            bgmVolume: state.bgmVolume
        };
    }

    function importSettingsData(data) {
        var source = data || {};
        state.effectEnabled = !(source.effectEnabled === false);
        state.effectVolume = clampVolume(source.effectVolume, state.effectVolume);
        state.bgmEnabled = !(source.bgmEnabled === false);
        state.bgmVolume = clampVolume(source.bgmVolume, state.bgmVolume);
    }

    function applySettingsToRuntime() {
        updateSettingsText();
        syncBgm();
        saveLocalSettings();
    }

    function loadSettingsFromServer() {
        if (!window.FarmApi || !$.isFunction(window.FarmApi.getCurUserSettings)) {
            return;
        }
        window.FarmApi.getCurUserSettings(function (res) {
            if (!(window.FarmApi.isOk(res) && res.data)) {
                return;
            }
            importSettingsData(res.data);
            applySettingsToRuntime();
            renderSettings();
        });
    }

    function saveSettingsToServer(onDone) {
        if (!window.FarmApi || !$.isFunction(window.FarmApi.saveCurUserSettings)) {
            if ($.isFunction(onDone)) {
                onDone(false);
            }
            return;
        }
        window.FarmApi.saveCurUserSettings(exportSettingsPayload(), function (res) {
            var ok = window.FarmApi.isOk(res);
            if (ok && res.data) {
                importSettingsData(res.data);
                applySettingsToRuntime();
                renderSettings();
            }
            if ($.isFunction(onDone)) {
                onDone(ok);
            }
        }, function () {
            if ($.isFunction(onDone)) {
                onDone(false);
            }
        });
    }

    function bindSettingsEvents() {
        $("#settingApplyBtn").off("click").on("click", function () {
            readSettingsForm();
            applySettingsToRuntime();
            saveSettingsToServer(function (saved) {
                $.messager.show({
                    title: "提示",
                    msg: saved ? "设置已保存到服务器" : "设置已在本地保存",
                    timeout: 1200,
                    showType: "slide"
                });
            });
            FarmAudio.play("click");
        });

        $("#settingEffectEnable").off("change").on("change", function () {
            readSettingsForm();
            applySettingsToRuntime();
            FarmAudio.play("click");
        });

        $("#settingBgmEnable").off("change").on("change", function () {
            readSettingsForm();
            applySettingsToRuntime();
            FarmAudio.play("click");
        });

        $("#settingEffectVolume").off("input change").on("input change", function () {
            readSettingsForm();
            applySettingsToRuntime();
        });

        $("#settingBgmVolume").off("input change").on("input change", function () {
            readSettingsForm();
            applySettingsToRuntime();
        });
    }

    FarmAudio.play = function (key) {
        if (!state.effectEnabled) {
            return;
        }
        var mainAudio = ensureAudio(key);
        if (!mainAudio) {
            return;
        }
        try {
            var audio = mainAudio.cloneNode();
            audio.volume = state.effectVolume;
            var promise = audio.play();
            if (promise && $.isFunction(promise.catch)) {
                promise.catch(function () {
                });
            }
        } catch (e) {
        }
    };

    FarmAudio.renderSettings = renderSettings;

    FarmAudio.setBgmUrl = function (url) {
        state.bgmUrl = $.trim(url || "");
        state.bgmEffectiveUrl = "";
        if (state.bgmAudio) {
            state.bgmAudio.pause();
            state.bgmAudio = null;
        }
        syncBgm();
    };

    FarmAudio.reloadSettings = function () {
        loadSettingsFromServer();
    };

    FarmAudio.init = function () {
        loadLocalSettings();
        $.each(keyToPath, function (key) {
            ensureAudio(key);
        });
        bindSettingsEvents();
        renderSettings();
        syncBgm();
        loadSettingsFromServer();
    };

    window.FarmAudio = FarmAudio;

    $(function () {
        FarmAudio.init();
    });
})(window, window.jQuery);

