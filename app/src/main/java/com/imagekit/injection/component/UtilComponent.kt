package com.imagekit.injection.component

import com.imagekit.ImageKit
import com.imagekit.ImagekitUrlConstructor
import com.imagekit.android.injection.module.ContextModule
import dagger.Component
import javax.inject.Singleton

@Component(modules = [(ContextModule::class)])
@Singleton
interface UtilComponent {
    fun inject(app: ImageKit)
    fun inject(imagekitUrlConstructor: ImagekitUrlConstructor)
}