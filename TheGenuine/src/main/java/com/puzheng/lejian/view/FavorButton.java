package com.puzheng.lejian.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.puzheng.deferred.DoneHandler;
import com.puzheng.deferred.FailHandler;
import com.puzheng.lejian.R;
import com.puzheng.lejian.model.SPU;
import com.puzheng.lejian.model.User;
import com.puzheng.lejian.store.FavorStore;
import com.puzheng.lejian.util.LoginRequired;

public class FavorButton extends ImageButton {
    public static final int LOGIN_ACTION = 1;
    private SPU spu;

    public FavorButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FavorButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void makeToast(int resId) {
        Toast.makeText(getContext(),
                getContext().getString(resId),
                Toast.LENGTH_SHORT).show();
    }

    public void setup(SPU spu, final int requestCode) {
        this.spu = spu;
        setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginRequired.with(getContext()).requestCode(requestCode)
                        .wraps(new LoginRequired.Runnable() {
                            @Override
                            public void run(User user) {
                                if (FavorButton.this.spu.isFavored()) {
                                    FavorStore.getInstance().unfavor(user, FavorButton.this.spu).done(new DoneHandler<Void>() {
                                        @Override
                                        public void done(Void aVoid) {
                                            makeToast(R.string.unfavor_succeed);
                                            FavorButton.this.spu.setFavored(false);
                                            update();
                                        }
                                    }).fail(new FailHandler<Void>() {
                                        @Override
                                        public void fail(Void aVoid) {
                                            makeToast(R.string.unfavor_failed);
                                        }
                                    });
                                } else {
                                    FavorStore.getInstance().favor(user, FavorButton.this.spu).done(new DoneHandler<Void>() {
                                        @Override
                                        public void done(Void aVoid) {
                                            makeToast(R.string.favor_succeed);
                                            FavorButton.this.spu.setFavored(true);
                                            update();
                                        }
                                    }).fail(new FailHandler<Void>() {
                                        @Override
                                        public void fail(Void aVoid) {
                                            makeToast(R.string.favor_failed);
                                        }
                                    });
                                }

                            }
                        });
            }
        });
        update();

    }

    private void update() {
        setImageResource(spu.isFavored() ? R.drawable.ic_action_important : R.drawable.ic_action_not_important);
    }

}
