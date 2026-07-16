# JetpackDemo 项目裁剪规划（Dagger / Hilt 学习专用）

> 分支：`hongwen-sgm-dagger2-only-trim-plan`
> 目标：只保留 Dagger 体系相关代码（本项目中体现为 **Hilt 模块**，Hilt 基于 Dagger2 构建，含 `@Module` / `@Provides` / `@Binds` / `@InstallIn` / `@Inject` / `@Singleton` 等注解），删除其他所有无关 Jetpack 演示模块，得到一个最小可编译、可运行、可学习的 Dagger 依赖注入工程。

---

## 1. 现状分析

### 1.1 关键发现
- 项目中**不存在独立的纯 Dagger2 示例代码**，`README.md` 里的 "Dagger2" 条目链接到另一个外部仓库 `ellisonchan/Dagger2Demo`。
- `app/build.gradle` 声明了 `com.google.dagger:dagger:2.33` + `dagger-compiler`，但源码中并没有 `@Component` / `@Subcomponent` 的原生 Dagger2 用法。
- 真正在使用 `dagger.*` 注解的代码全部位于 **`com.ellison.jetpackdemo.hilt`** 包，使用的是 Hilt（`@HiltAndroidApp` / `@AndroidEntryPoint` / `@Module` + `@InstallIn` / `@Provides` / `@Binds` 等）。
- 因此，保留 Hilt 模块 = 保留项目中唯一存在的、真实可运行的 Dagger 生态示例。

### 1.2 现有模块清单（`com.ellison.jetpackdemo`）
| 包 | 说明 | 是否与 Dagger 相关 | 处置 |
| --- | --- | --- | --- |
| `hilt/` | Hilt DI 完整示例（Activity / Fragment / BroadcastReceiver / ViewModel / Module）| ✅ 是 | **保留** |
| `MyApplication.kt` | `@HiltAndroidApp` 入口 | ✅ 是 | **保留（需精简 CameraX 相关代码）** |
| `MainActivity.kt` | 各 Demo 入口菜单 | ⚠️ 部分 | **保留但改写：只保留跳转 Hilt Demo 的按钮** |
| `appCompat/` | AppCompat 演示 | ❌ | 删除 |
| `camera2/` | Camera2 演示 | ❌ | 删除 |
| `cameraX/` | CameraX 演示 | ❌ | 删除 |
| `coroutines/` | Coroutines 演示 | ❌ | 删除 |
| `databinding/` | DataBinding 演示 | ❌ | 删除 |
| `lifecycle/` | Lifecycle 演示 | ⚠️ `MyApplication` 引用了 `ActivityLifecycleCallbackImpl` | 删除该类，同时清理 `MyApplication` 的引用 |
| `liveData/` | LiveData 演示 | ❌ | 删除 |
| `old/` | 旧版 Activity 演示 | ❌ | 删除 |
| `room/` | Room 独立演示 | ❌ | 删除（Hilt 内部有自己的 Room 使用，与该目录无耦合） |
| `viewBinding/` | ViewBinding 演示 | ❌ | 删除 |
| `viewModel/` | ViewModel 演示 | ❌ | 删除 |
| `viewModelBinding/` | ViewModel + DataBinding | ❌ | 删除 |

### 1.3 Hilt 模块内部结构（全部保留）
```
hilt/
├── BaseActivity.kt              // @AndroidEntryPoint 基类
├── DemoActivity.kt              // 入口 Activity（@AndroidEntryPoint）
├── DemoFragment.kt              // @AndroidEntryPoint Fragment
├── DemoBroadcastReceiver.kt     // @AndroidEntryPoint Receiver
├── bean/
│   ├── Movie.kt
│   └── MovieResponse.kt
├── model/
│   ├── LocalData.kt             // @Inject 构造
│   ├── RemoteData.kt            // @Inject 构造
│   ├── Repository.kt            // @Inject 构造 + 组合上面两者
│   ├── analysis/
│   │   ├── AnalyseService.kt
│   │   └── AnalysisModule.kt    // @Module + @Binds + @InstallIn(ActivityComponent)
│   ├── database/
│   │   ├── MovieDao.kt
│   │   ├── NewMovieDataBase.kt
│   │   └── RoomModule.kt        // @Module + @Provides + @InstallIn(ApplicationComponent)
│   ├── interceptor/
│   │   ├── CallServerInterceptorOkHttpClient.kt   // @Qualifier
│   │   └── LoggingInterceptorOkHttpClient.kt      // @Qualifier
│   └── network/
│       ├── NetworkService.kt
│       └── NetworkModule.kt     // @Module + @Provides + @InstallIn(ApplicationComponent)
├── view/
│   └── MovieAdapter.kt          // @Inject + @ActivityContext
└── viewmodel/
    └── MovieViewModel.kt        // @HiltViewModel / @Inject
```
Hilt 是 Dagger 的官方封装，其中 `@Module` / `@Provides` / `@Binds` / `@Qualifier` / `@Inject` / `@Singleton` 都是原生 Dagger2 概念，非常适合作为学习入口。

### 1.4 资源与配置依赖
Hilt 模块运行时依赖的 res 资源：
- `layout/activity_hilt.xml`、`activity_hilt_item.xml`、`fragment_hilt.xml`
- `layout/activity_main.xml`（改写后只保留 Hilt 按钮）
- `menu/`、`values/`、`mipmap-*`、`drawable*` 中被 `activity_main.xml`、Hilt 布局引用到的部分
- `raw/`（如无引用则删除）

`AndroidManifest.xml` 中需要保留的组件：
- `MyApplication`
- `MainActivity`
- `.hilt.DemoActivity`
- 其余 `<activity>` 全部删除
- `CAMERA / RECORD_AUDIO` 等与 CameraX/Camera2 相关的权限删除，保留 `INTERNET`（Retrofit 需要）

---

## 2. 裁剪方案

### 2.1 总体策略
1. **保留最小可运行工程**：`MyApplication` + `MainActivity`（单按钮）+ `hilt/*` 整棵子树。
2. **依赖精简**：`build.gradle` 只留 Kotlin、AppCompat、Material、ConstraintLayout、Lifecycle、ViewModel、Room（Hilt 用到）、Retrofit + OkHttp + Gson、Hilt、`androidx.hilt`；纯 Dagger 的 `com.google.dagger:dagger` + `dagger-compiler` 明确保留（供后续手写 Dagger2 学习使用）。
3. **构建配置对齐**：保留 `kotlin-kapt`、`dagger.hilt.android.plugin`、`viewBinding`、`dataBinding`（Hilt Demo 内 `ActivityHiltBinding` 是 ViewBinding，`activity_hilt.xml` 若无 `<layout>` 标签实际只需 ViewBinding；保留 dataBinding 以防止关联布局报错，后续可再收敛）。
4. **权限与组件**：`AndroidManifest.xml` 只保留 `INTERNET`、`MyApplication`、`MainActivity`、`.hilt.DemoActivity`。
5. **README / doc**：新增 `README.md` 顶部说明"本分支为 Dagger/Hilt 学习裁剪版"，保留原 Dagger2 章节链接。
6. **git 保留 `.git`、`.gitignore`、`gradle/`、`gradlew`、`gradlew.bat`、`settings.gradle`、`build.gradle`、`LICENSE`**。

### 2.2 需要删除的清单

#### 源码目录（`app/src/main/java/com/ellison/jetpackdemo/`）
- `appCompat/`
- `camera2/`
- `cameraX/`
- `coroutines/`
- `databinding/`
- `lifecycle/`
- `liveData/`
- `old/`
- `room/`
- `viewBinding/`
- `viewModel/`
- `viewModelBinding/`

#### 测试目录
- `app/src/androidTest/`、`app/src/test/` 中除与 Hilt 相关的用例外全部清空（当前应无 Hilt 专属测试，全部清理）。

#### 资源文件（`app/src/main/res/`）
| 目录/文件 | 处置 |
| --- | --- |
| `layout/activity_camera2.xml` | 删 |
| `layout/activity_camerax_lite.xml` | 删 |
| `layout/activity_coroutines.xml` | 删 |
| `layout/activity_data_binding*.xml` | 删 |
| `layout/activity_lifecycle.xml` | 删 |
| `layout/activity_live_data.xml` | 删 |
| `layout/activity_main_test*.xml` | 删 |
| `layout/activity_old.xml` | 删 |
| `layout/activity_room_db*.xml` | 删 |
| `layout/activity_view_binding.xml` | 删 |
| `layout/activity_view_model*.xml` | 删 |
| `layout/fragment_analysis_list.xml` / `analysis_list_item.xml` | 保留（AnalysisModule 演示用） |
| `layout/fragment_other_view_model.xml`, `fragment_view_binding.xml`, `fragment_view_model.xml` | 删 |
| `layout-land/` | 逐个比对；未被 Hilt/Main 布局引用则整体删 |
| `menu/` | 若仅被删除模块使用则删 |
| `values-night/` | 保留（Theme 需要） |
| `raw/` | 若无引用则删 |
| `drawable/`, `drawable-v24/`, `mipmap-*` | 保留 Icon 及 Hilt/Main 使用到的图片；删除只被 CameraX/Room/AppCompat 引用的资源 |

#### 依赖（`app/build.gradle`）
删除以下条目：
- `androidx.camera:*`
- `com.google.zxing:core`
- `com.huawei.hms:scanplus`
- `androidx.preference:preference*`
- `androidx.room:room-guava` / `room-rxjava2` / `room-testing`（Hilt 只用 `room-runtime` + `room-ktx` + `room-compiler`）
- 保留：`androidx.appcompat`、`material`、`constraintlayout`、`lifecycle-*`、`core-ktx`、`fragment-ktx`、`kotlinx-coroutines-android`、`retrofit` + `converter-gson` + `okhttp3:logging-interceptor`、Hilt 相关全部、Dagger 相关全部、Room `runtime/ktx/compiler`。

#### Manifest（`app/src/main/AndroidManifest.xml`）
删除除以下之外的所有 `<activity>` 和权限：
- `.MainActivity`（LAUNCHER）
- `.hilt.DemoActivity`
- 权限只留 `INTERNET`

#### 顶层
- `README.md`：保留但顶部添加"本分支已裁剪为 Dagger/Hilt 学习专用"的说明段落。

### 2.3 需要改写的文件
1. **`MyApplication.kt`**：
   - 删除 `CameraXConfig.Provider` 实现和 `getCameraXConfig()`；
   - 删除对 `lifecycle.ActivityLifecycleCallbackImpl` 的引用；
   - 保留 `@HiltAndroidApp` 与最小 `onCreate`。
2. **`MainActivity.kt`**：
   - 仅保留 `showHiltDemo(view)` 方法；
   - 删除其他 `showXxxDemo` 方法。
3. **`activity_main.xml`**：
   - 仅保留 `hilt_btn` 按钮，其它按钮全部删除。
4. **`app/build.gradle`**：按 §2.2 精简依赖。
5. **`AndroidManifest.xml`**：按 §2.2 精简。

### 2.4 校验清单
- [ ] `./gradlew :app:assembleDebug` 通过
- [ ] APK 安装后点击 "Hilt" 按钮能正常进入 `DemoActivity` 并触发一次搜索
- [ ] `com.ellison.jetpackdemo` 包下只剩 `MyApplication.kt`、`MainActivity.kt`、`hilt/`
- [ ] `grep` 项目内不再存在对已删除包类的 import
- [ ] IDE 不报未解析资源

### 2.5 可选（后续扩展）
若要补一份**纯 Dagger2**（`@Component` / `@Subcomponent`）示例，建议在 `com.ellison.jetpackdemo.dagger2/` 下另建包，基于现有的 `Movie / Repository / RemoteData / LocalData` 复用即可，与 Hilt 部分并存，便于对比学习。

---

## 3. 执行阶段划分

| 阶段 | 任务 | 产出 |
| --- | --- | --- |
| 0 | 备份/确认分支 `hongwen-sgm-dagger2-only-trim-plan` | 当前分支 |
| 1 | 删除 §2.2 列出的源码包 | 源码树清爽 |
| 2 | 精简 `MyApplication`、`MainActivity`、`activity_main.xml` | 编译主入口通过 |
| 3 | 清理 `AndroidManifest.xml` 权限与组件 | 只剩两个 Activity |
| 4 | 精简 `app/build.gradle` 依赖 | Gradle sync 成功 |
| 5 | 清理未引用的 `res/` 资源 | Lint 无未使用告警 |
| 6 | 执行 `./gradlew :app:assembleDebug` 验证 | 构建通过 |
| 7 | 手动跑一次 Hilt Demo 冒烟 | 运行通过 |
| 8 | 更新 `README.md` 顶部说明 | 文档同步 |
| 9 | 提交 & 推送 | PR 准备就绪 |

---

## 4. 风险与注意事项
1. **资源交叉引用**：部分 `drawable / string / color / style` 可能被 `activity_main.xml` 与被删布局同时引用，删除前建议先跑一次 Lint 或使用 Android Studio 的 "Remove Unused Resources"。
2. **Theme 依赖**：`Theme.JetpackDemoFullScreen` / `Theme.JetpackDemoOld` 只被待删 Activity 使用，可从 `values/styles.xml` 中删除；`Theme.JetpackDemo` 必须保留。
3. **kapt 版本**：Hilt `2.28-alpha` 与 Dagger `2.33` 并存目前可用；如后续升级 Hilt，注意 `ApplicationComponent` 已在新版本改名为 `SingletonComponent`。
4. **测试目录**：如果保留 `androidTest`，建议加一条最简单的 Hilt `@HiltAndroidTest`，否则整体删除。

---

## 5. 完成后目录预期
```
JetpackDemo/
├── app/
│   ├── build.gradle              # 精简后
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml   # 只留 MainActivity + hilt.DemoActivity
│       ├── java/com/ellison/jetpackdemo/
│       │   ├── MyApplication.kt
│       │   ├── MainActivity.kt   # 只留 Hilt 按钮回调
│       │   └── hilt/             # 完整保留
│       └── res/                  # 精简后
├── build.gradle                  # 保持
├── settings.gradle
├── gradle/、gradlew*、README.md、LICENSE
└── doc/
    └── dagger-trim-plan.md       # 本文
```
