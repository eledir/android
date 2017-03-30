/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * THIS FILE WAS GENERATED BY codergen. EDIT WITH CARE.
 */
package com.android.tools.idea.editors.gfxtrace.service.gfxapi;

import com.android.tools.rpclib.schema.Entity;
import com.android.tools.rpclib.schema.Field;
import com.android.tools.rpclib.schema.Method;
import com.android.tools.rpclib.schema.Primitive;
import com.android.tools.rpclib.schema.Slice;
import org.jetbrains.annotations.NotNull;

import com.android.tools.rpclib.binary.*;

import java.io.IOException;

public final class IndexBuffer implements BinaryObject {
  //<<<Start:Java.ClassBody:1>>>
  private int[] myIndices;

  // Constructs a default-initialized {@link IndexBuffer}.
  public IndexBuffer() {}


  public int[] getIndices() {
    return myIndices;
  }

  public IndexBuffer setIndices(int[] v) {
    myIndices = v;
    return this;
  }

  @Override @NotNull
  public BinaryClass klass() { return Klass.INSTANCE; }


  private static final Entity ENTITY = new Entity("gfxapi", "IndexBuffer", "", "");

  static {
    ENTITY.setFields(new Field[]{
      new Field("Indices", new Slice("", new Primitive("uint32", Method.Uint32))),
    });
    Namespace.register(Klass.INSTANCE);
  }
  public static void register() {}
  //<<<End:Java.ClassBody:1>>>
  public enum Klass implements BinaryClass {
    //<<<Start:Java.KlassBody:2>>>
    INSTANCE;

    @Override @NotNull
    public Entity entity() { return ENTITY; }

    @Override @NotNull
    public BinaryObject create() { return new IndexBuffer(); }

    @Override
    public void encode(@NotNull Encoder e, BinaryObject obj) throws IOException {
      IndexBuffer o = (IndexBuffer)obj;
      e.uint32(o.myIndices.length);
      for (int i = 0; i < o.myIndices.length; i++) {
        e.uint32(o.myIndices[i]);
      }
    }

    @Override
    public void decode(@NotNull Decoder d, BinaryObject obj) throws IOException {
      IndexBuffer o = (IndexBuffer)obj;
      o.myIndices = new int[d.uint32()];
      for (int i = 0; i <o.myIndices.length; i++) {
        o.myIndices[i] = d.uint32();
      }
    }
    //<<<End:Java.KlassBody:2>>>
  }
}