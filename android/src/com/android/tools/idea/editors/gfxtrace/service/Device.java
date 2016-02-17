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
package com.android.tools.idea.editors.gfxtrace.service;

import com.android.tools.rpclib.schema.*;
import com.android.tools.rpclib.binary.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public final class Device implements BinaryObject {
  //<<<Start:Java.ClassBody:1>>>
  private String myName;
  private String myModel;
  private String myOS;
  private byte myPointerSize;
  private byte myPointerAlignment;
  private long myMaxMemorySize;
  private String myExtensions;
  private String myRenderer;
  private String myVendor;
  private String myVersion;

  // Constructs a default-initialized {@link Device}.
  public Device() {}


  public String getName() {
    return myName;
  }

  public Device setName(String v) {
    myName = v;
    return this;
  }

  public String getModel() {
    return myModel;
  }

  public Device setModel(String v) {
    myModel = v;
    return this;
  }

  public String getOS() {
    return myOS;
  }

  public Device setOS(String v) {
    myOS = v;
    return this;
  }

  public byte getPointerSize() {
    return myPointerSize;
  }

  public Device setPointerSize(byte v) {
    myPointerSize = v;
    return this;
  }

  public byte getPointerAlignment() {
    return myPointerAlignment;
  }

  public Device setPointerAlignment(byte v) {
    myPointerAlignment = v;
    return this;
  }

  public long getMaxMemorySize() {
    return myMaxMemorySize;
  }

  public Device setMaxMemorySize(long v) {
    myMaxMemorySize = v;
    return this;
  }

  public String getExtensions() {
    return myExtensions;
  }

  public Device setExtensions(String v) {
    myExtensions = v;
    return this;
  }

  public String getRenderer() {
    return myRenderer;
  }

  public Device setRenderer(String v) {
    myRenderer = v;
    return this;
  }

  public String getVendor() {
    return myVendor;
  }

  public Device setVendor(String v) {
    myVendor = v;
    return this;
  }

  public String getVersion() {
    return myVersion;
  }

  public Device setVersion(String v) {
    myVersion = v;
    return this;
  }

  @Override @NotNull
  public BinaryClass klass() { return Klass.INSTANCE; }


  private static final Entity ENTITY = new Entity("service", "Device", "", "");

  static {
    ENTITY.setFields(new Field[]{
      new Field("Name", new Primitive("string", Method.String)),
      new Field("Model", new Primitive("string", Method.String)),
      new Field("OS", new Primitive("string", Method.String)),
      new Field("PointerSize", new Primitive("uint8", Method.Uint8)),
      new Field("PointerAlignment", new Primitive("uint8", Method.Uint8)),
      new Field("MaxMemorySize", new Primitive("uint64", Method.Uint64)),
      new Field("Extensions", new Primitive("string", Method.String)),
      new Field("Renderer", new Primitive("string", Method.String)),
      new Field("Vendor", new Primitive("string", Method.String)),
      new Field("Version", new Primitive("string", Method.String)),
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
    public BinaryObject create() { return new Device(); }

    @Override
    public void encode(@NotNull Encoder e, BinaryObject obj) throws IOException {
      Device o = (Device)obj;
      e.string(o.myName);
      e.string(o.myModel);
      e.string(o.myOS);
      e.uint8(o.myPointerSize);
      e.uint8(o.myPointerAlignment);
      e.uint64(o.myMaxMemorySize);
      e.string(o.myExtensions);
      e.string(o.myRenderer);
      e.string(o.myVendor);
      e.string(o.myVersion);
    }

    @Override
    public void decode(@NotNull Decoder d, BinaryObject obj) throws IOException {
      Device o = (Device)obj;
      o.myName = d.string();
      o.myModel = d.string();
      o.myOS = d.string();
      o.myPointerSize = d.uint8();
      o.myPointerAlignment = d.uint8();
      o.myMaxMemorySize = d.uint64();
      o.myExtensions = d.string();
      o.myRenderer = d.string();
      o.myVendor = d.string();
      o.myVersion = d.string();
    }
    //<<<End:Java.KlassBody:2>>>
  }
}