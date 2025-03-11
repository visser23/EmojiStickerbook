@echo off
echo Updating package name from com.example.storybookemoji to io.github.storybookemoji...
echo.

echo Creating new directory structure...
mkdir app\src\main\java\io\github\storybookemoji
mkdir app\src\main\java\io\github\storybookemoji\model
mkdir app\src\main\java\io\github\storybookemoji\ui
mkdir app\src\main\java\io\github\storybookemoji\ui\components
mkdir app\src\main\java\io\github\storybookemoji\ui\screens
mkdir app\src\main\java\io\github\storybookemoji\ui\theme

mkdir app\src\androidTest\java\io\github\storybookemoji
mkdir app\src\test\java\io\github\storybookemoji

echo.
echo Copying and updating files...
echo.

echo Updating MainActivity.kt...
powershell -Command "(Get-Content app\src\main\java\com\example\storybookemoji\MainActivity.kt) -replace 'package com.example.storybookemoji', 'package io.github.storybookemoji' -replace 'import com.example.storybookemoji', 'import io.github.storybookemoji' | Set-Content app\src\main\java\io\github\storybookemoji\MainActivity.kt"

echo Updating model files...
powershell -Command "(Get-Content app\src\main\java\com\example\storybookemoji\model\EmojiSticker.kt) -replace 'package com.example.storybookemoji', 'package io.github.storybookemoji' -replace 'import com.example.storybookemoji', 'import io.github.storybookemoji' | Set-Content app\src\main\java\io\github\storybookemoji\model\EmojiSticker.kt"
powershell -Command "(Get-Content app\src\main\java\com\example\storybookemoji\model\PageData.kt) -replace 'package com.example.storybookemoji', 'package io.github.storybookemoji' -replace 'import com.example.storybookemoji', 'import io.github.storybookemoji' | Set-Content app\src\main\java\io\github\storybookemoji\model\PageData.kt"

echo Updating UI components...
powershell -Command "(Get-Content app\src\main\java\com\example\storybookemoji\ui\components\BookPage.kt) -replace 'package com.example.storybookemoji', 'package io.github.storybookemoji' -replace 'import com.example.storybookemoji', 'import io.github.storybookemoji' | Set-Content app\src\main\java\io\github\storybookemoji\ui\components\BookPage.kt"
powershell -Command "(Get-Content app\src\main\java\com\example\storybookemoji\ui\components\DraggableEmoji.kt) -replace 'package com.example.storybookemoji', 'package io.github.storybookemoji' -replace 'import com.example.storybookemoji', 'import io.github.storybookemoji' | Set-Content app\src\main\java\io\github\storybookemoji\ui\components\DraggableEmoji.kt"
powershell -Command "(Get-Content app\src\main\java\com\example\storybookemoji\ui\components\EmojiSelector.kt) -replace 'package com.example.storybookemoji', 'package io.github.storybookemoji' -replace 'import com.example.storybookemoji', 'import io.github.storybookemoji' | Set-Content app\src\main\java\io\github\storybookemoji\ui\components\EmojiSelector.kt"

echo Updating UI screens...
powershell -Command "(Get-Content app\src\main\java\com\example\storybookemoji\ui\screens\BookScreen.kt) -replace 'package com.example.storybookemoji', 'package io.github.storybookemoji' -replace 'import com.example.storybookemoji', 'import io.github.storybookemoji' | Set-Content app\src\main\java\io\github\storybookemoji\ui\screens\BookScreen.kt"

echo Updating UI theme...
powershell -Command "(Get-Content app\src\main\java\com\example\storybookemoji\ui\theme\Color.kt) -replace 'package com.example.storybookemoji', 'package io.github.storybookemoji' -replace 'import com.example.storybookemoji', 'import io.github.storybookemoji' | Set-Content app\src\main\java\io\github\storybookemoji\ui\theme\Color.kt"
powershell -Command "(Get-Content app\src\main\java\com\example\storybookemoji\ui\theme\Theme.kt) -replace 'package com.example.storybookemoji', 'package io.github.storybookemoji' -replace 'import com.example.storybookemoji', 'import io.github.storybookemoji' | Set-Content app\src\main\java\io\github\storybookemoji\ui\theme\Theme.kt"
powershell -Command "(Get-Content app\src\main\java\com\example\storybookemoji\ui\theme\Type.kt) -replace 'package com.example.storybookemoji', 'package io.github.storybookemoji' -replace 'import com.example.storybookemoji', 'import io.github.storybookemoji' | Set-Content app\src\main\java\io\github\storybookemoji\ui\theme\Type.kt"

echo Updating test files...
powershell -Command "(Get-Content app\src\test\java\com\example\storybookemoji\ExampleUnitTest.kt) -replace 'package com.example.storybookemoji', 'package io.github.storybookemoji' -replace 'import com.example.storybookemoji', 'import io.github.storybookemoji' | Set-Content app\src\test\java\io\github\storybookemoji\ExampleUnitTest.kt"
powershell -Command "(Get-Content app\src\androidTest\java\com\example\storybookemoji\ExampleInstrumentedTest.kt) -replace 'package com.example.storybookemoji', 'package io.github.storybookemoji' -replace 'import com.example.storybookemoji', 'import io.github.storybookemoji' -replace 'assertEquals(\"com.example.storybookemoji\"', 'assertEquals(\"io.github.storybookemoji\"' | Set-Content app\src\androidTest\java\io\github\storybookemoji\ExampleInstrumentedTest.kt"

echo.
echo Package name update complete!
echo.
echo IMPORTANT: You should now:
echo 1. Build the project to ensure everything works correctly
echo 2. Delete the old package directories (app\src\main\java\com\example\storybookemoji\*)
echo.

pause 