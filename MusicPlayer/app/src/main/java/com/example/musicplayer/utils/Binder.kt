package com.example.musicplayer.utils

import android.view.LayoutInflater
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

fun <T : ViewDataBinding> Fragment.memberBinding(
    inflate: (LayoutInflater) -> T,
    onDestroy: T.() -> Unit = {}
) = Binder(this, inflate, onDestroy)

class Binder<T : ViewDataBinding>(
    private val fragment: Fragment,
    private val inflate: (LayoutInflater) -> T,
    private val onDestroy: T.() -> Unit
) : ReadOnlyProperty<Fragment, T>, LifecycleObserver {
    private var fragmentBinding: T? = null

    init {
        fragment.observeOwnerThroughCreation {
            lifecycle.addObserver(this@Binder)
        }
    }

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        assertMainThread()

        val binding = fragmentBinding

        if (binding != null) {
            return binding
        }

        val lifecycle = fragment.viewLifecycleOwner.lifecycle

        check(lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED)) {
            "Fragment views are destroyed."
        }

        return inflate(thisRef.requireContext().inflater).also {
            fragmentBinding = it
        }
    }

    private inline fun Fragment.observeOwnerThroughCreation(
        crossinline viewOwner: LifecycleOwner.() -> Unit
    ) {
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                super.onCreate(owner)

                viewLifecycleOwnerLiveData.observe(this@observeOwnerThroughCreation) {
                    it.viewOwner()
                }
            }
        })
    }

    @Suppress("UNUSED")
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun destroy() {
        fragmentBinding?.onDestroy()
        fragmentBinding = null
    }
}